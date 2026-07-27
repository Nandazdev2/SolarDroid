display.setDefault("background", 0.05, 0.05, 0.1)

printf("Testando widget com baseDir corrigido")

local ok, err = pcall(function()
    local widget = require("widget")
    widget.setTheme("widget_theme_android_holo_dark")
    local myButton = widget.newButton({
        left = display.contentCenterX - 60, top = 100, width = 120, height = 40,
        label = "Testar", fontSize = 14,
        onRelease = function() printf("Botao clicado!") end,
    })
    local mySwitch = widget.newSwitch({
        left = display.contentCenterX - 20, top = 170,
        style = "onOff",
    })
end)

if ok then
    printf("SUCESSO: widget renderizado!")
else
    printf("FALHOU: " .. tostring(err))
end
