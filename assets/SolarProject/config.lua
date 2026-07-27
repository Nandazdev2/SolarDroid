-- config.lua
-- Coloque na mesma pasta do main.lua

application = {
    content = {
        width = 1080,           -- Largura da tela
        height = 2338,          -- Altura da tela (sua resolução)
        scale = "letterbox",    -- Mantém proporção sem distorcer
        fps = 60,               -- 60 FPS para mais fluidez
        
        -- Outras opções de scale:
        -- "letterbox" = mantém proporção com barras pretas
        -- "zoomEven"  = preenche toda tela mantendo proporção (corta bordas)
        -- "none"      = estica para preencher (pode distorcer)
        
        -- Opções de FPS:
        -- 30 = 30 FPS (economia de bateria)
        -- 60 = 60 FPS (mais fluido, recomendado)
        -- 120 = 120 FPS (apenas dispositivos compatíveis)
        
        -- Centralizar na tela
        xAlign = "center",
        yAlign = "center",
        
        -- Anti-aliasing (qualidade gráfica)
        antialias = true,
        
        -- Cores
        graphicsCompatibility = 1,  -- 1 = OpenGL ES 1.1, 2 = OpenGL ES 2.0
    },
    
    -- Configurações de notificação (opcional)
    notification = {
        iphone = {
            types = {
                "badge", "sound", "alert"
            }
        }
    },
    
    -- Licença (opcional)
    license = {
        google = {
            key = "sua-chave-google-play",  -- Remova se não for usar
        },
    },
}