package jp.sf.amateras.stepcounter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 各種ユーティリティメソッドを提供するクラス
 */
public class Util {

	private static String fileEncoding = null;
	private static FileEncodingDetector fileEncodingDetector = null;

	public static final String IGNORE_GENERATED_FILE = "ignore.generated.file";
	public static final String EXTENSION_PAIRS = "extension.pairs";
	public static final String IGNORE_FILENAME_PATTERNS = "ignore.filename.patterns";
	public static final String FILENAME_PATTERNS = "filename.patterns";
	public static final String EXTENSION_SEPARATOR = ",";
	public static final String PAIR_SEPARATOR = "|";
	public static final String PAIR_SEPARATOR_REGEX = "\\|";
	public static final String FILENAME_PATTERN_SEPARATOR = "|||";
	public static final String FILENAME_PATTERN_SEPARATOR_REGEX = "\\|\\|\\|";
	
	/**
	 * 文字列を指定文字列で分割し、配列で返却します。
	 *
	 * @param str 文字列
	 * @param del 区切り文字列
	 * @return 分割された文字列を格納した配列
	 */
	public static String[] split(String str,String del){
		ArrayList<String> list = new ArrayList<String>();
		int pos   = 0;
		int index = 0;
		while((index=str.indexOf(del,pos))!=-1){
			list.add(str.substring(pos,index));
			pos = index + del.length();
		}
		list.add(str.substring(pos,str.length()));
		return (String[])list.toArray(new String[list.size()]);
	}

	/**
	 * 渡された文字列を指定エンコーディングの指定バイト数で先頭から切り出す。
	 * カタカナの判定は正しく行うことができない。
	 *
	 * @param   s    切り出し対象文字列
	 * @param   cnt  切り出しバイト数
	 * @return  結果文字列
	 */
	public static String substring(String str,int length){
		String resultStr = null;
		int zenCnt      = 0;
//		int kisuuFlg    = 0;
		int loopCnt     = length;
		byte[] resBytes = new byte[length];
		byte[] bytes    = str.getBytes();
		// 指定バイト数以下の場合はそのまま返却
		if(bytes.length <= length) {
			return str;
		}
		for (int i=0; i < length; i++) {
			if (bytes[i] < 0) {
				// bytes[i]の8ビット目が立っている(全角)
				zenCnt ++;
			}
		}
		// 全角バイトの数が奇数の場合
		if(zenCnt % 2 == 1) {
			loopCnt--;
		}
		for(int i=0; i < loopCnt ; i++) {
			resBytes[i] = bytes[i];
		}
		resultStr = new String(resBytes);
		return resultStr;
	}

	/**
	 * 引数で渡した文字列のバイト長を返します。
	 *
	 * @param str 文字列
	 * @return バイト長
	 */
	public static int getByteLength(String str){
		try {
			byte[] bytes = str.getBytes();
			return bytes.length;
		} catch(Exception ex){
			return str.getBytes().length;
		}
	}

	/**
	 * HTML/XMLの特殊文字を実態参照に変換します。
	 *
	 * @param str 文字列
	 * @return 変換後の文字列
	 */
	public static String escapeXML(String str){
		str.replaceAll("&" ,"&amp;");
		str.replaceAll("<" ,"&gt;");
		str.replaceAll(">" ,"&lt;");
		str.replaceAll("\"","&quot;");
		return str;
	}

	/**
	 * ストリームを強制的にクローズします。
	 *
	 * @param closeable ストリーム
	 */
	public static void close(Closeable closeable){
		if(closeable != null){
			try {
				closeable.close();
			} catch(Exception ex){
				;
			}
		}
	}

	public static void setFileEncodingDetector(FileEncodingDetector detector){
		fileEncodingDetector = detector;
	}

	public static void setFileEncoding(String encoding){
		fileEncoding = encoding;
	}

	public static String getFileEncoding(File file){
		if(fileEncoding != null){
			return fileEncoding;
		}

		if(fileEncodingDetector != null){
			String encoding = fileEncodingDetector.getEncoding(file);
			if(encoding != null){
				return encoding;
			}
		}

		return System.getProperty("file.encoding");
	}
	
	public static String[] exceptGeneratedFile(String[] filePaths) {
		return exceptGeneratedFile(null, filePaths);
	}
	
	public static File[] exceptGeneratedFile(File[] files) {
		return exceptGeneratedFile(null, files);
	}
	
	public static String[] exceptGeneratedFile(String basePath, String[] filePaths) {
		if (!Boolean.getBoolean(IGNORE_GENERATED_FILE)) {
			return filePaths;
		}
		if (filePaths == null) {
			return filePaths;
		}
		File[] files = new File[filePaths.length];
		for(int i=0; i<files.length; i++) {
			files[i] = new File(filePaths[i]);
		}
		File basePathFile = basePath == null ? null : new File(basePath);
		File[] exceptedFiles = exceptGeneratedFile(basePathFile, files);
		String[] exceptedPaths = new String[exceptedFiles.length];
		for (int i=0; i<exceptedPaths.length; i++) {
			exceptedPaths[i] = exceptedFiles[i].getPath();
		}
		return exceptedPaths;
	}
	
	public static Map<String, String> createExtensionPairs() {
		Map<String, String> pairs = new LinkedHashMap<String, String>();
		String source = System.getProperty(EXTENSION_PAIRS);
		if (source == null || source.length() == 0) {
			return pairs;
		}
		String[] rows = source.split(PAIR_SEPARATOR_REGEX);
		for(String row : rows) {
			String[] values = row.split(EXTENSION_SEPARATOR);
			if (values.length >= 2) {
				pairs.put(values[0], values[1]);
			}
		}
		return pairs;
	}

	public static boolean ignoreFilenamePatterns() {
		return Boolean.getBoolean(IGNORE_FILENAME_PATTERNS);
	}
	
	public static boolean ignoreGeneratedFile() {
		return Boolean.getBoolean(IGNORE_GENERATED_FILE);
	}
	
	public static String getExtensionPairsString() {
		return System.getProperty(EXTENSION_PAIRS);
	}
	
	public static String getFilenamePatternsString() {
		return System.getProperty(FILENAME_PATTERNS);
	}
	
	public static File[] exceptGeneratedFile(File basePathFile, File[] files) {
		if (!ignoreGeneratedFile()) {
			return files;
		}
		if (files == null) {
			return files;
		}
		Map<String, String> extensionPairs = createExtensionPairs();
		List<File> fileList = new ArrayList<File>(Arrays.asList(files));
		List<File> priors = gatherPriorExtensionFiles(extensionPairs, basePathFile, files);
		for (Iterator<File> itr = fileList.iterator(); itr.hasNext();) {
			File file = itr.next();
			if (!(basePathFile == null ? file.isFile() : new File(basePathFile, file.getPath()).isFile())) {
				continue;
			}
			String fileExtension = selectExtension(file);
			if (extensionPairs.containsValue(fileExtension)) {
				String filePath = file.getPath();
				String filePathWithoutExtension = filePath.substring(0, filePath.length() - fileExtension.length());
				for (Entry<String, String> extensionPair : extensionPairs.entrySet()) {
					if (!fileExtension.equals(extensionPair.getValue())) {
						continue;
					}
					if (priors.contains(new File(filePathWithoutExtension + extensionPair.getKey()))) {
						itr.remove();
						break;
					}
				}
			}
		}
		return fileList.toArray(new File[fileList.size()]);
	}

	private static List<File> gatherPriorExtensionFiles(Map<String, String> extensionPairs, File basePathFile, File[] files) {
		List<File> priors = new ArrayList<File>();
		for(File file : files) {
			if (basePathFile == null ? file.isFile() : new File(basePathFile, file.getPath()).isFile()) {
				String extension = selectExtension(file);
				if (extensionPairs.containsKey(extension)) {
					priors.add(file);
				}
			}
		}
		return priors;
	}

	private static String selectExtension(File file) {
		String path = file.getPath();
		int lastPeriodIndex = path.lastIndexOf('.');
		return lastPeriodIndex > 0 ? path.substring(lastPeriodIndex) : "";
	}
	
	public static boolean matchToAny(Pattern[] patterns, File parent, File file) {
		if (!Util.ignoreFilenamePatterns()) {
			return false;
		}
		File target = parent == null ? file : new File(parent, file.getPath());
		if (!target.isFile()) {
			return false;
		}
		String path;
		try {
			path = target.getCanonicalPath();
		} catch (IOException e) {
			path = target.getAbsolutePath();
		}
		return matchToAny(patterns, path);
	}
	
	public static boolean matchToAny(Pattern[] patterns, String value) {
		for(Pattern pattern : patterns) {
			if (pattern.matcher(value).matches()) {
				return true;
			}
		}
		return false;
	}
	
	public static Pattern[] createFilenamePatterns() {
		String filenamePatternsString = System.getProperty(FILENAME_PATTERNS);
		if (filenamePatternsString == null) {
			return new Pattern[0];
		}
		String[] filenamePatterns = filenamePatternsString.split(FILENAME_PATTERN_SEPARATOR_REGEX);
		return createPatterns(filenamePatterns);
	}
	
	public static Pattern[] createPatterns(String[] patternStrings) {
		if (patternStrings == null) 
			return new Pattern[0];
		if (patternStrings.length == 1 && patternStrings[0].trim().length() == 0) {
			return new Pattern[0];
		}
		List<Pattern> patterns = new ArrayList<Pattern>(patternStrings.length);
		for(String regex : patternStrings) {
			try {
				patterns.add(Pattern.compile(regex));
			} catch (PatternSyntaxException e) {
				System.err.println("Ignore regex pattern:\"" + regex + "\" : cause by exception as follows");
				e.printStackTrace();
			}
		}
		return patterns.toArray(new Pattern[patterns.size()]);
	}
}
