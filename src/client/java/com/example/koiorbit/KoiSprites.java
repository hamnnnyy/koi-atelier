package com.example.koiorbit;

// 후광과 날개 도트 그림. 날개는 왼쪽 열이 등에 붙는 쪽, 위쪽이 위
// W L M D = 색을 입히는 밝기 단계, K = 검은 테두리, S = 흰색 (색 안 입힘)
public final class KoiSprites {
    public static final KoiSprite ANGEL = new KoiSprite(
            "..........MMMM..",
            "......MMMMWWWWM.",
            "...MMMWWWWWWWWWM",
            ".MMWWWWWWWWWWWWM",
            "MWWWWLLWWWLLWWWM",
            "MWWLLWWWLLWWWLWM",
            "MLLWWWWLWWWWLWWM",
            ".MWWWWLWWWWLWWWM",
            ".MWWWLWWWWLWWWM.",
            "..MWLWWWWLWWWWM.",
            "..MWLWWWLWWWWM..",
            "...MWWWLWWWWWM..",
            "...MWWLWWWWWM...",
            "....MWLWWMWM....",
            "....MWLWM.M.....",
            ".....MM.........");

    public static final KoiSprite BUTTERFLY = new KoiSprite(
            "....KKKKKKK.",
            "..KKWWWWWWWK",
            ".KWWWLLWWWWK",
            "KWWWLSSLWWWK",
            "KWWWLSSLWWK.",
            "KWWWWLLWWWK.",
            "KMWWWWWWWK..",
            ".KKMMMMKK...",
            ".KWWWWWK....",
            "KWWLSLWWK...",
            "KWWWLWWWK...",
            "KMWWWWWK....",
            ".KMMMKK.....",
            "..KKK.......");

    public static final KoiSprite DRAGON = new KoiSprite(
            "..............KK",
            "..........KKKKMD",
            "......KKKKMMMMMD",
            "..KKKKMMKKMMMMMD",
            "KKMMMMMKMKKMMMMD",
            "DMMMMMMKMKMKKMMD",
            "DMMMMMMKMKMMMKMD",
            "DMMMMMMKMMKMMMKD",
            "DMMMMMKMMMKMMDDK",
            "DDMMMMKMMMKMD...",
            "..DMMMKMMMKMD...",
            "...DMMKDDDDK....",
            "....DKD....K....",
            ".....K..........");

    // 위에서 내려다본 고리 (반대편은 그늘지게)
    public static final KoiSprite HALO = new KoiSprite(
            "...LWWWL...",
            ".WW.....WL.",
            ".W.......W.",
            "L.........L",
            "W.........W",
            "W.........W",
            "W.........W",
            "L.........L",
            ".W.......M.",
            ".LW.....MM.",
            "...MMMMM...");

    private KoiSprites() {
    }
}
