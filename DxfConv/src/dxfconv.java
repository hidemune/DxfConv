
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;
import javax.swing.JOptionPane;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hdm
 */
public class dxfconv {
public static StringBuilder sbSrc = null;
public static StringBuilder sbDest = null;
public static StringBuilder sbConv = null;
public static dxfConvJFrame dxfconvF;
public static boolean isSJisEsc = false;
public static boolean isUnicEsc = false;
public static File fileSrc = null;
public static String DirPath = "";
public static String Replace = "";
public static String[] strReplaceBefore = null;
public static String[] strReplaceAfter = null;
public static boolean blnStopMsg = false;

private static final char[] ZENKAKU_KATAKANA = { 'ァ', 'ア', 'ィ', 'イ', 'ゥ',
    'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ',
    'コ', 'ゴ', 'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ',
    'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ', 'ト', 'ド', 'ナ', 'ニ', 'ヌ',
    'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ',
    'ペ', 'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ',
    'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ', 'ヮ', 'ワ', 'ヰ', 'ヱ', 'ヲ', 'ン',
    'ヴ', 'ヵ', 'ヶ' };
private static final String[] HANKAKU_KATAKANA = { "ｧ", "ｱ", "ｨ", "ｲ", "ｩ",
    "ｳ", "ｪ", "ｴ", "ｫ", "ｵ", "ｶ", "ｶﾞ", "ｷ", "ｷﾞ", "ｸ", "ｸﾞ", "ｹ",
    "ｹﾞ", "ｺ", "ｺﾞ", "ｻ", "ｻﾞ", "ｼ", "ｼﾞ", "ｽ", "ｽﾞ", "ｾ", "ｾﾞ", "ｿ",
    "ｿﾞ", "ﾀ", "ﾀﾞ", "ﾁ", "ﾁﾞ", "ｯ", "ﾂ", "ﾂﾞ", "ﾃ", "ﾃﾞ", "ﾄ", "ﾄﾞ",
    "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ", "ﾊ", "ﾊﾞ", "ﾊﾟ", "ﾋ", "ﾋﾞ", "ﾋﾟ", "ﾌ",
    "ﾌﾞ", "ﾌﾟ", "ﾍ", "ﾍﾞ", "ﾍﾟ", "ﾎ", "ﾎﾞ", "ﾎﾟ", "ﾏ", "ﾐ", "ﾑ", "ﾒ",
    "ﾓ", "ｬ", "ﾔ", "ｭ", "ﾕ", "ｮ", "ﾖ", "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ", "ﾜ",
    "ﾜ", "ｲ", "ｴ", "ｦ", "ﾝ", "ｳﾞ", "ｶ", "ｹ" };
private static final char ZENKAKU_KATAKANA_FIRST_CHAR = ZENKAKU_KATAKANA[0];
private static final char ZENKAKU_KATAKANA_LAST_CHAR = ZENKAKU_KATAKANA[ZENKAKU_KATAKANA.length - 1];

public static String version = "1.0.5";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        dxfconvF = new dxfConvJFrame();
        
        //プロパティファイルの読み込み
        Properties config = new Properties();
        try {
            //config.load(new FileInputStream("eijiro.properties"));
            config.load(new InputStreamReader(new FileInputStream("dxfconv.properties"), "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        DirPath = config.getProperty("DirPath", "");
        
        Replace = config.getProperty("Replace", "");
        String[] split = Replace.split(",");
        strReplaceBefore = new String[split.length];
        strReplaceAfter = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
            String[] str = split[i].split("/");
            if (split[i].indexOf("/") > 0) {
                strReplaceBefore[i] = str[0];
                strReplaceAfter[i] = str[1];
            }
        }
        
        dxfconvF.setVisible(true);
    }
    
    public static void ZenHan() {
        //全角カナを半角ｶﾅに変換
        StringBuilder sb = new StringBuilder();
        String[] strs = sbDest.toString().split("\n");
        // 変換
        for (int j = 0; j < sbDest.length(); j++) {
          char originalChar = sbDest.charAt(j);
          String convertedChar = zenkakuKatakanaToHankakuKatakana(originalChar);
          sb.append(convertedChar);
        }
        sbDest = sb;
        //画面に表示
        dxfconvF.initListDest();
        String[] strs2 = sbDest.toString().split("\n");
        for (int i = 0 ; i < strs2.length ; i++){
            String line = strs2[i];
            // 日本語を含む行は画面に表示
            if (isZenkaku(line) && !line.equals("")) {
                dxfconvF.setListDest(line);
            }
        }
    }
    public static String zenkakuKatakanaToHankakuKatakana(char c) {
      if (c >= ZENKAKU_KATAKANA_FIRST_CHAR && c <= ZENKAKU_KATAKANA_LAST_CHAR) {
        return HANKAKU_KATAKANA[c - ZENKAKU_KATAKANA_FIRST_CHAR];
      } else {
        return String.valueOf(c);
      }
    }

    public static void writeProp() {
        //プロパティファイルの書き込み
        Properties config = new Properties();
        
        config.setProperty("DirPath", DirPath);;
        config.setProperty("Replace", Replace);;
        
        try {
            config.store(new OutputStreamWriter(new FileOutputStream("dxfconv.properties"),"UTF-8"), "by HDM");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void saveDxf() {
        //ファイルの書き込み
        try {
            File file = new File(dxfconvF.getFileNameDest());
            //古いファイルのバックアップ
            if (file.exists()) {
                File fileB = new File(file.getAbsolutePath() + "~");
                if (fileB.exists()) {
                    fileB.delete();
                }
                file.renameTo(fileB);
            }
            // 常に新規作成
            PrintWriter bw;
            if (dxfconvF.getDestEnc() == 0) {
                bw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")));
            }else {
                //SJISは機種依存文字や全角ハイフンが文字化けする
                bw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"MS932")));
            }
            
            //書き込み
            String[] strs = sbConv.toString().split("\n");
            for (int i = 0 ; i < strs.length ; i++){
                String line = strs[i];
                //Replace : 行頭から行末まで完全に一致する場合のみ置換
                for (int j = 0; j < strReplaceBefore.length; j++) {
                    if (line.equals(strReplaceBefore[j])) {
                        line = strReplaceAfter[j];
                        break;
                    }
                }
                bw.print(line);
                bw.println();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            // Fileオブジェクト生成時の例外捕捉
            e.printStackTrace();
            JOptionPane.showMessageDialog(dxfconvF, "エラーが発生しました");
            return;
        } catch (IOException e) {
            // BufferedWriterオブジェクトのクローズ時の例外捕捉
            e.printStackTrace();
            JOptionPane.showMessageDialog(dxfconvF, "エラーが発生しました");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dxfconvF, "エラーが発生しました");
            return;
        }
        
        if (!blnStopMsg) {
            JOptionPane.showMessageDialog(dxfconvF, "DXFファイルを更新しました");
        }
    }
    
    public static void setSrc(File file){
        fileSrc = file;
        //メモリ初期化
        sbSrc = new StringBuilder();
        sbDest = null;
        sbConv = null;
        isSJisEsc = false;
        isUnicEsc = false;
        
        //DXFファイルの読み込み
        try {
            BufferedReader br;
            if (dxfconvF.getSrcEnc() == 0) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            }else {
                //SJIS
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "MS932"));
            }
            // 最終行まで読み込む
            String line = "";
            dxfconvF.initListSrc();
            dxfconvF.initListDest();
            dxfconvF.initListConv();
            //dxfconvF.setListCnvSrc("開始");
            while ((line = br.readLine()) != null) {
                // メモリに退避
                sbSrc.append(line);
                sbSrc.append("\n");
                // 日本語を含む行は画面に表示
                if (isZenkaku(line) && !line.equals("")) {
                    dxfconvF.setListSrc(line);
                }
            }
            br.close();
            
            //可読文字列に変換
            SJisEscConv();
            
            //dxfconvF.setListCnvSrc("終了");
            if (!blnStopMsg) {
                JOptionPane.showMessageDialog(dxfconvF, "終了しました。\n文字化けしている時は読み込み文字コードを変更して\n再読み込みしてください。", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void setConv() {
        //文字エスケープを行う
        if (dxfconvF.getConvType() == 0) {
            //可読文字列はそのまま
            sbConv = sbDest;
            dxfconvF.ModelConv = dxfconvF.ModelDest;
            dxfconvF.setModelConv();
        } else {
            //通常文字列をUnicodeエスケープ文字列に変換
            sbConv = new StringBuilder();
            String[] strs = sbDest.toString().split("\n");
            for (int i = 0 ; i < strs.length ; i++){
                String line = strs[i];
                //System.out.println(line); 
                StringBuilder sbLine = new StringBuilder();
                int idx = 0;
                while (true) {
                    if (line.length() <= idx) {
                        break;
                    }
                    //1文字ずつ処理：全角半角判定
                    String str = line.substring(idx, idx + 1);
                    //if ((str.getBytes().length) != str.length()) {
                    try {
                        byte [] bytes = str.getBytes("UTF-16");
                        int code = (int)bytes[2] * 0x100 + (int)bytes[3];
                        if ((code < 0x0020) || (0x007E < code)) {
                            //Unicodeエスケープに変換
                                sbLine.append("\\U+");
                                sbLine.append(Character.forDigit(bytes[2] >> 4 & 0xF, 16));
                                sbLine.append(Character.forDigit(bytes[2] & 0xF, 16));
                                sbLine.append(Character.forDigit(bytes[3] >> 4 & 0xF, 16));
                                sbLine.append(Character.forDigit(bytes[3] & 0xF, 16));
                        } else {
                            sbLine.append(str);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    idx = idx + 1;
                }
                sbConv.append(sbLine.toString());
                sbConv.append("\n");
            }
            //画面に表示
            dxfconvF.initListConv();
            String[] strs2 = sbConv.toString().split("\n");
            for (int i = 0 ; i < strs2.length ; i++){
                String line = strs2[i];
                // 日本語を含む行は画面に表示
                if (isZenkaku(line) && !line.equals("")) {
                    dxfconvF.setListConv(line);
                }
            }
        }
    }
    
    public static void SJisEscConv() {
        if (!isSJisEsc && !isUnicEsc) {
            sbDest = sbSrc;
            System.out.println("false:Not Encode");
            //画面に表示
            dxfconvF.initListDest();
            dxfconvF.ModelDest = dxfconvF.ModelSrc;
            dxfconvF.setModelDest();
        } else {
            if (isSJisEsc){
                System.out.println("true:SJIS");
                //SJISエスケープ文字列を通常文字列に変換
                sbDest = new StringBuilder();
                String[] strs = sbSrc.toString().split("\n");
                for (int i = 0 ; i < strs.length ; i++){
                    String line = strs[i];
                    //System.out.println(line); 
                    StringBuilder sbLine = new StringBuilder();
                    int frmIdx = 0;
                    while (true) {
                        int idx = line.indexOf("\\M+1", frmIdx);
                        if (idx < frmIdx) {
                            //残りの文字列を吐き出す
                            sbLine.append(line.substring(frmIdx));
                            break;
                        } else {
                            //前の文字を吐き出す
                            if (idx > frmIdx) {
                                sbLine.append(line.substring(frmIdx, idx));
                            }
                            // \M+12345 ８文字
                            frmIdx = idx + 8;
                            //コード変換
                            try {
                                String codeStr = line.substring(idx, idx + 8);
                                String jisCodeStr = codeStr.substring(4);
                                //System.out.println(jisCodeStr);
                                byte[] jiscode = new byte[2];
                                jiscode[0] = (byte)Integer.parseInt(jisCodeStr.substring(0, 2),16);
                                jiscode[1] = (byte)Integer.parseInt(jisCodeStr.substring(2, 4),16);
                                String str = new String(jiscode, "MS932");
                                //System.out.println(str);
                                sbLine.append(str);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    sbDest.append(sbLine.toString());
                    sbDest.append("\n");
                }
                //次の処理のため
                //sbSrc = new StringBuilder(sbDest.toString());
                sbSrc =sbDest;
            }
            if (isUnicEsc){
                System.out.println("true:Unicode");
                //Unicodeエスケープ文字列を通常文字列に変換
                sbDest = new StringBuilder();
                String[] strs2 = sbSrc.toString().split("\n");
                System.out.println(strs2.length);
                for (int i = 0 ; i < strs2.length ; i++){
                    String line = strs2[i];
                    //System.out.println(line); 
                    StringBuilder sbLine = new StringBuilder();
                    int frmIdx = 0;
                    while (true) {
                        int idx = line.indexOf("\\U+", frmIdx);
                        if (idx < frmIdx) {
                            //残りの文字列を吐き出す
                            sbLine.append(line.substring(frmIdx));
                            break;
                        } else {
                            //前の文字を吐き出す
                            if (idx > frmIdx) {
                                sbLine.append(line.substring(frmIdx, idx));
                            }
                            // \U+1234 7文字
                            frmIdx = idx + 7;
                            //コード変換
                            try {
                                String codeStr = line.substring(idx, idx + 7);
                                String jisCodeStr = codeStr.substring(3);
                                //System.out.println(jisCodeStr);
                                byte[] jiscode = new byte[2];
                                jiscode[0] = (byte)Integer.parseInt(jisCodeStr.substring(0, 2),16);
                                jiscode[1] = (byte)Integer.parseInt(jisCodeStr.substring(2, 4),16);
                                String str = new String(jiscode, "UTF-16");
                                //System.out.println(str);
                                sbLine.append(str);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    sbDest.append(sbLine.toString());
                    sbDest.append("\n");
                }
            }
            //画面に表示
            dxfconvF.initListDest();
            String[] strs3 = sbDest.toString().split("\n");
            System.out.println(strs3.length);
            for (int i = 0 ; i < strs3.length ; i++){
                String line = strs3[i];
                // 日本語を含む行は画面に表示
                if (isZenkaku(line) && !line.equals("")) {
                    dxfconvF.setListDest(line);
                }
            }
        }
    }
    
    public static boolean isZenkaku ( String str ){
        // \U+4EEE のパターン
        if (str.matches(".*\\\\U\\+.*")) {
            isUnicEsc = true;
            return true;
        }
//        if (str.matches(".*\\\\u\\+.*")) {
//            isUnicEsc = true;
//            return true;
//        }
        // \M+189BC のパターン
        if (str.matches(".*\\\\M\\+.*")) {
            //System.out.println("SJisEnc:");
            isSJisEsc = true;
            return true;
        }
//        if (str.matches(".*\\\\m\\+.*")) {
//            //System.out.println("SJisEnc:");
//            isSJisEsc = true;
//            return true;
//        }
        // １文字でも全角文字含めば
        if ((str.getBytes().length) != str.length()) {
            //System.out.println("Zenkaku:");
            return true;
        }
        return false;
    }
}
