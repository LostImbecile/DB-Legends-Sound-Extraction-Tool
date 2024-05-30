package processing;

public class FileType {
    public static boolean isVoice(String path) {
        return path.contains(".voice") && !path.contains("bgm_");
    }

    public static boolean isJPN(String path) {
        return path.contains("voice_jpn") && !path.contains("bgm_");
    }

    public static boolean isEng(String path) {
        return path.contains("voice_eng") && !path.contains("bgm_");
    }

    public static boolean isSoundEffect(String path) {
        return path.contains("_se_") || path.contains("cmn") || path.contains("detailed")
                || path.contains("sys_bundle");
    }

    public static int parseID(String characterID) {
        return Integer.parseInt(characterID.replaceFirst("^0+(?!$)", ""));
    }
}
