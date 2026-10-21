package com.example.koiorbit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Mod Menu가 설치돼 있으면 모드 목록의 설정 버튼으로 설정 화면 열기
public class KoiModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return KoiOrbitClient::createConfigScreen;
    }
}
