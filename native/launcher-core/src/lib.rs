use serde::Serialize;
use serde_json::{from_slice, json, to_vec, Value};
use std::fs::File;
use std::io::{Cursor, Read, Seek};
use std::mem::forget;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::ptr::{null_mut, slice_from_raw_parts_mut};
use std::slice;
use std::str::from_utf8;
use zip::{result::ZipError, ZipArchive};

const MAX_DEPTH: usize = 4;
const MAX_ARCHIVES: usize = 2048;
const MAX_ARCHIVE_ENTRIES: usize = 100_000;
const MAX_METADATA_ENTRY_BYTES: u64 = 256 * 1024;
const MAX_METADATA_BYTES: usize = 4 * 1024 * 1024;
const MAX_JSON_DEPTH: usize = 48;
const MAX_NESTED_ENTRIES: usize = 512;
const MAX_NESTED_ENTRY_BYTES: u64 = 128 * 1024 * 1024;
const MAX_NESTED_BYTES: u64 = 256 * 1024 * 1024;
const MAX_PATH_BYTES: usize = 32768;
const MAX_RESPONSE_BYTES: usize = 4 * 1024 * 1024;

#[derive(Serialize)]
struct Identity { id: String, name: String, links: Vec<String> }
#[derive(Default)]
struct Budget { archives: usize, bytes: u64, metadata: usize, identities: Vec<Identity> }
type ScanResult<T> = Result<T, String>;
fn bounded(reader: impl Read, limit: u64) -> ScanResult<Vec<u8>> {
    let mut bytes = Vec::new();
    reader.take(limit + 1).read_to_end(&mut bytes).map_err(|e| e.to_string())?;
    if bytes.len() as u64 > limit { return Err("Archive entry exceeds its size budget".into()); }
    Ok(bytes)
}
fn check_depth(value: &Value, level: usize) -> ScanResult<()> {
    if level >= MAX_JSON_DEPTH && (value.is_object() || value.is_array()) { return Err("Mod metadata nesting exceeds its budget".into()); }
    match value {
        Value::Object(items) => for item in items.values() { check_depth(item, level + 1)?; },
        Value::Array(items) => for item in items { check_depth(item, level + 1)?; },
        _ => (),
    }
    Ok(())
}
fn scan<R: Read + Seek>(reader: R, depth: usize, budget: &mut Budget) -> ScanResult<()> {
    budget.archives += 1;
    if depth > MAX_DEPTH || budget.archives > MAX_ARCHIVES { return Err("Nested archive budget exceeded".into()); }
    let mut zip = ZipArchive::new(reader).map_err(|e| e.to_string())?;
    if zip.len() > MAX_ARCHIVE_ENTRIES { return Err("Too many archive entries".into()); }
    let metadata = match zip.by_name("fabric.mod.json") {
        Ok(entry) => bounded(entry, MAX_METADATA_ENTRY_BYTES)?,
        Err(ZipError::FileNotFound) => return Ok(()),
        Err(e) => return Err(e.to_string()),
    };
    budget.metadata += metadata.len();
    if budget.metadata > MAX_METADATA_BYTES { return Err("Total mod metadata exceeds its budget".into()); }
    let root: Value = from_slice(&metadata).map_err(|e| e.to_string())?;
    check_depth(&root, 0)?;
    let id = root["id"].as_str().ok_or("Missing Fabric mod ID")?;
    if !(2..=64).contains(&id.len()) || !id.as_bytes()[0].is_ascii_lowercase()
        || !id.bytes().all(|b| b.is_ascii_lowercase() || b.is_ascii_digit() || b == b'_' || b == b'-') {
        return Err("Invalid Fabric mod ID".into());
    }
    let name = match root.get("name") { None | Some(Value::Null) => id, Some(v) => v.as_str().ok_or("Invalid mod name")? };
    let links = root.get("contact").and_then(Value::as_object).map(|c| c.values().filter_map(Value::as_str).map(str::to_owned).collect()).unwrap_or_default();
    budget.identities.push(Identity { id: id.into(), name: name.into(), links });
    if let Some(jars) = root.get("jars") {
        let jars = jars.as_array().ok_or("Invalid bundled mod list")?;
        if jars.len() > MAX_NESTED_ENTRIES { return Err("Too many bundled mods".into()); }
        for jar in jars {
            let name = jar["file"].as_str().ok_or("Missing bundled mod filename")?;
            let entry = zip.by_name(name).map_err(|e| e.to_string())?;
            if entry.size() > MAX_NESTED_ENTRY_BYTES { return Err("Nested mod is too large".into()); }
            let bytes = bounded(entry, MAX_NESTED_ENTRY_BYTES)?;
            budget.bytes += bytes.len() as u64;
            if budget.bytes > MAX_NESTED_BYTES { return Err("Bundled mod data is too large".into()); }
            scan(Cursor::new(bytes), depth + 1, budget)?;
        }
    }
    Ok(())
}
fn scan_path(path: &str) -> ScanResult<Vec<Identity>> {
    let file = File::open(path).map_err(|e| e.to_string())?;
    let mut budget = Budget::default();
    scan(file, 0, &mut budget)?;
    Ok(budget.identities)
}

#[no_mangle]
pub extern "C" fn tool_core_abi() -> u32 { 1 }
#[no_mangle]
pub unsafe extern "C" fn tool_core_scan(path: *const u8, length: usize, output_length: *mut usize) -> *mut u8 {
    if output_length.is_null() { return null_mut(); }
    *output_length = 0;
    if path.is_null() || length == 0 || length > MAX_PATH_BYTES { return null_mut(); }
    let result = catch_unwind(AssertUnwindSafe(|| -> ScanResult<Vec<Identity>> {
        let path = from_utf8(slice::from_raw_parts(path, length)).map_err(|e| e.to_string())?;
        scan_path(path)
    }));
    let response = match result {
        Ok(Ok(mods)) => json!({"mods": mods}),
        Ok(Err(error)) => json!({"error": error}),
        Err(_) => json!({"error": "Native archive scanner failed"}),
    };
    let Ok(bytes) = to_vec(&response) else { return null_mut(); };
    if bytes.len() > MAX_RESPONSE_BYTES { return null_mut(); }
    let mut boxed = bytes.into_boxed_slice(); *output_length = boxed.len();
    let pointer = boxed.as_mut_ptr(); forget(boxed); pointer
}
#[no_mangle]
pub unsafe extern "C" fn tool_core_free(pointer: *mut u8, length: usize) {
    if !pointer.is_null() && length > 0 { drop(Box::from_raw(slice_from_raw_parts_mut(pointer, length))); }
}
