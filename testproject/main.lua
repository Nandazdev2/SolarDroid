display.setDefault("background", 0.05, 0.05, 0.1)

local title = display.newText("Teste 2: sem physics", display.contentCenterX, 40, native.systemFont, 20)
title:setFillColor(1, 1, 1)

print("Passo 1 OK: basico funcionou")

local ok, err = pcall(function()
    local widget = require("widget")
    print("Passo 2 OK: widget carregado")

    local myButton = widget.newButton({
        left = display.contentCenterX - 60,
        top = 100,
        width = 120,
        height = 40,
        label = "Testar",
        fontSize = 14,
        onRelease = function() print("Botao clicado!") end,
    })
end)

if not ok then
    local errText = display.newText("ERRO WIDGET: " .. tostring(err), display.contentCenterX, 160, native.systemFont, 12)
    errText:setFillColor(1, 0.3, 0.3)
    print("ERRO widget: " .. tostring(err))
else
    local okText = display.newText("Widget OK!", display.contentCenterX, 160, native.systemFont, 16)
    okText:setFillColor(0.3, 1, 0.3)
end

local circle = display.newCircle(display.contentCenterX, 250, 30)
circle:setFillColor(0.3, 0.7, 1)
transition.to(circle, {rotation=360, time=2000, iterations=-1})

print("Passo 3 OK: script terminou de rodar")
