use serde::Serialize;
use serde_json::Value;
use std::{fs::File, io::{Cursor, Read, Seek}, panic::{catch_unwind, AssertUnwindSafe}, slice};
use zip::ZipArchive;

#[derive(Serialize)]
struct Identity { id: String, name: String, links: Vec<String> }
#[derive(Default)]
struct Budget { archives: usize, bytes: u64, metadata: usize, identities: Vec<Identity> }
type Result<T> = std::result::Result<T, String>;
fn bounded(reader: impl Read, limit: u64) -> Result<Vec<u8>> {
    let mut bytes = Vec::new();
    reader.take(limit + 1).read_to_end(&mut bytes).map_err(|e| e.to_string())?;
    if bytes.len() as u64 > limit { return Err("Archive entry exceeds its size budget".into()); }
    Ok(bytes)
}
fn check_depth(value: &Value, level: usize) -> Result<()> {
    if level >= 48 && (value.is_object() || value.is_array()) { return Err("Mod metadata nesting exceeds its budget".into()); }
    match value {
        Value::Object(items) => for item in items.values() { check_depth(item, level + 1)?; },
        Value::Array(items) => for item in items { check_depth(item, level + 1)?; },
        _ => (),
    }
    Ok(())
}
fn scan<R: Read + Seek>(reader: R, depth: usize, budget: &mut Budget) -> Result<()> {
    budget.archives += 1;
    if depth > 4 || budget.archives > 2048 { return Err("Nested archive budget exceeded".into()); }
    let mut zip = ZipArchive::new(reader).map_err(|e| e.to_string())?;
    if zip.len() > 100000 { return Err("Too many archive entries".into()); }
    let metadata = match zip.by_name("fabric.mod.json") {
        Ok(entry) => bounded(entry, 256 * 1024)?,
        Err(zip::result::ZipError::FileNotFound) => return Ok(()),
        Err(e) => return Err(e.to_string()),
    };
    budget.metadata += metadata.len();
    if budget.metadata > 4 * 1024 * 1024 { return Err("Total mod metadata exceeds its budget".into()); }
    let root: Value = serde_json::from_slice(&metadata).map_err(|e| e.to_string())?;
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
        if jars.len() > 512 { return Err("Too many bundled mods".into()); }
        for jar in jars {
            let name = jar["file"].as_str().ok_or("Missing bundled mod filename")?;
            let entry = zip.by_name(name).map_err(|e| e.to_string())?;
            if entry.size() > 128 * 1024 * 1024 { return Err("Nested mod is too large".into()); }
            let bytes = bounded(entry, 128 * 1024 * 1024)?;
            budget.bytes += bytes.len() as u64;
            if budget.bytes > 256 * 1024 * 1024 { return Err("Bundled mod data is too large".into()); }
            scan(Cursor::new(bytes), depth + 1, budget)?;
        }
    }
    Ok(())
}
#[no_mangle]
pub extern "C" fn tool_core_abi() -> u32 { 1 }
#[no_mangle]
pub unsafe extern "C" fn tool_core_scan(path: *const u8, length: usize, output_length: *mut usize) -> *mut u8 {
    if output_length.is_null() { return std::ptr::null_mut(); }
    *output_length = 0;
    if path.is_null() || length == 0 || length > 32768 { return std::ptr::null_mut(); }
    let result = catch_unwind(AssertUnwindSafe(|| -> Result<Vec<Identity>> {
        let path = std::str::from_utf8(slice::from_raw_parts(path, length)).map_err(|e| e.to_string())?;
        let file = File::open(path).map_err(|e| e.to_string())?;
        let mut budget = Budget::default(); scan(file, 0, &mut budget)?; Ok(budget.identities)
    }));
    let response = match result {
        Ok(Ok(mods)) => serde_json::json!({"mods": mods}),
        Ok(Err(error)) => serde_json::json!({"error": error}),
        Err(_) => serde_json::json!({"error": "Native archive scanner failed"}),
    };
    let Ok(bytes) = serde_json::to_vec(&response) else { return std::ptr::null_mut(); };
    if bytes.len() > 4 * 1024 * 1024 { return std::ptr::null_mut(); }
    let mut boxed = bytes.into_boxed_slice(); *output_length = boxed.len();
    let pointer = boxed.as_mut_ptr(); std::mem::forget(boxed); pointer
}
#[no_mangle]
pub unsafe extern "C" fn tool_core_free(pointer: *mut u8, length: usize) {
    if !pointer.is_null() && length > 0 { drop(Box::from_raw(std::ptr::slice_from_raw_parts_mut(pointer, length))); }
}
