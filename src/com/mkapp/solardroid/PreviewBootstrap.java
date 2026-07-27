package com.mkapp.solardroid;

public class PreviewBootstrap {

    public static final String LUA_CODE =
        "local _origNewImage = display.newImage\n" +
        "local _origNewImageRect = display.newImageRect\n" +
        "local _origNewImageSheet = graphics.newImageSheet\n" +
        "\n" +
        "local function _hasNoBaseDir(args, baseDirIndex)\n" +
        "    local v = args[baseDirIndex]\n" +
        "    return v == nil or type(v) ~= \"userdata\"\n" +
        "end\n" +
        "\n" +
        "display.newImage = function(...)\n" +
        "    local args = {...}\n" +
        "    local ok, result = pcall(_origNewImage, ...)\n" +
        "    if ok and result then return result end\n" +
        "    local n = select(\"#\", ...)\n" +
        "    if n >= 1 then\n" +
        "        local newArgs = {}\n" +
        "        for i = 1, n do newArgs[i] = select(i, ...) end\n" +
        "        table.insert(newArgs, 2, system.DocumentsDirectory)\n" +
        "        local ok2, result2 = pcall(_origNewImage, table.unpack(newArgs))\n" +
        "        if ok2 and result2 then return result2 end\n" +
        "    end\n" +
        "    return result\n" +
        "end\n" +
        "\n" +
        "display.newImageRect = function(...)\n" +
        "    local args = {...}\n" +
        "    local ok, result = pcall(_origNewImageRect, ...)\n" +
        "    if ok and result then return result end\n" +
        "    local n = select(\"#\", ...)\n" +
        "    local newArgs = {}\n" +
        "    for i = 1, n do newArgs[i] = select(i, ...) end\n" +
        "    local insertPos = 2\n" +
        "    if type(newArgs[1]) == \"table\" then insertPos = 3 end\n" +
        "    table.insert(newArgs, insertPos, system.DocumentsDirectory)\n" +
        "    local ok2, result2 = pcall(_origNewImageRect, table.unpack(newArgs))\n" +
        "    if ok2 and result2 then return result2 end\n" +
        "    return result\n" +
        "end\n" +
        "\n" +
        "graphics.newImageSheet = function(filename, ...)\n" +
        "    local args = {...}\n" +
        "    local ok, result = pcall(_origNewImageSheet, filename, ...)\n" +
        "    if ok and result then return result end\n" +
        "    local ok2, result2 = pcall(_origNewImageSheet, filename, system.DocumentsDirectory, ...)\n" +
        "    if ok2 and result2 then return result2 end\n" +
        "    return result\n" +
        "end\n";
}
