package jp.sf.amateras.stepcounter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import jp.sf.amateras.stepcounter.format.FormatterFactory;
import jp.sf.amateras.stepcounter.format.ResultFormatter;


/** �R�}���h���C������̋N���N���X */
public class Main {

	private File[] files;
	private ResultFormatter formatter;
	private OutputStream output = System.out;
	private boolean showDirectory = false;

	private Pattern[] filenamePatterns;
	
	/** �����Ŏw�肵���f�B���N�g������̊K�w��\�����邩�ݒ肵�܂� */
	public void setShowDirectory(boolean showDirectory) {
		this.showDirectory = showDirectory;
	}

	/** �t�@�C�����Z�b�g���܂� */
	public void setFiles(File[] files){
		this.files = files;
	}

	/** �t�H�[�}�b�^���Z�b�g���܂��B */
	public void setFormatter(ResultFormatter formatter){
		this.formatter = formatter;
	}

	/** �o�̓X�g���[����ݒ肵�܂��B */
	public void setOutput(OutputStream output){
		this.output = output;
	}

	/** �J�E���g�����s���܂� */
	public void executeCount() throws IOException {
		// �t�H�[�}�b�^���ݒ肳��Ă��Ȃ��ꍇ�̓f�t�H���g���g�p
		if(formatter == null){
			formatter = FormatterFactory.getFormatter("");
		}
		filenamePatterns = Util.createFilenamePatterns();
		// �P�t�@�C�� or �P�f�B���N�g�����J�E���g
		ArrayList<CountResult> list = new ArrayList<CountResult>();
		for(int i=0;i<files.length;i++){
			CountResult[] results = count(files[i]);
			for(int j=0;j<results.length;j++){
				list.add(results[j]);
			}
		}
		CountResult[] results = (CountResult[])list.toArray(new CountResult[list.size()]);
		if (this.showDirectory) {
			for (CountResult result : results) {
				// �����f�B���N�g���t���̃t�@�C�����ɏ㏑�����܂��B
				result.setFileName(getFileNameWithDir(result.getFile()));
			}
		}
		output.write(formatter.format(results));
		output.flush();
		if(output != System.out){
			output.close();
		}
	}

	/** �P�t�@�C�����J�E���g */
	private CountResult[] count(File file) throws IOException {
		if(file.isDirectory()){
			File[] files = Util.exceptGeneratedFile(null, file.listFiles());
			ArrayList<CountResult> list = new ArrayList<CountResult>();
			for(int i=0;i<files.length;i++){
				CountResult[] results = count(files[i]);
				for(int j=0;j<results.length;j++){
					list.add(results[j]);
				}
			}
			return (CountResult[])list.toArray(new CountResult[list.size()]);
		} else {
			if(Util.matchToAny(filenamePatterns, null, file)) {
				return new CountResult[0];
			}
			StepCounter counter = StepCounterFactory.getCounter(file.getName());
			if(counter!=null){
				CountResult result = counter.count(file, Util.getFileEncoding(file));
				return new CountResult[]{result};
			} else {
				// ���Ή��̌`���̏ꍇ�͌`����null��ݒ肵�ĕԂ�
				return new CountResult[]{
					new CountResult(file, file.getName(), null, null, 0, 0, 0)
				};
			}
		}
	}

	/** �f�B���N�g���t���t�@�C�����̏o�͌`�����擾���܂��B */
	private String getFileNameWithDir(File file) throws IOException {
		if (file.isDirectory()) {
			return file.getName();
		}
		if (this.files == null || this.files.length == 0) {
			return file.getName();
		}
		// �t�@�C���̐��K�p�X���擾���܂��B
		String filePath = file.getCanonicalPath();
		for (File f : this.files) {
			String parentPath = f.getCanonicalPath();
			if (filePath.contains(parentPath)) {
				// �����̐��K�p�X�Ƀt�@�C�����܂܂�Ă���ꍇ�A
				// �I�����ꂽ�f�B���N�g������̃p�X�ƃt�@�C������ԋp���܂��B
				StringBuilder builder = new StringBuilder();
				builder.append('/');
				builder.append(f.getName());
				builder.append(filePath.substring(parentPath.length()).replaceAll("\\\\", "/"));
				return builder.toString();
			}
		}
		return file.getName();
	}

	/** �R�}���h���C���N���p���\�b�h */
	public static void main(String[] args) throws IOException {

		if(args==null || args.length==0){
			System.exit(0);
		}
		String format = null;
		String output = null;
		String encoding = null;
		String showDirectory = null;
		ArrayList<File> fileList = new ArrayList<File>();
		for(int i=0;i<args.length;i++){
			if(args[i].startsWith("-format=")){
				String[] dim = Util.split(args[i],"=");
				format = dim[1];
			} else if(args[i].startsWith("-output=")){
				String[] dim = Util.split(args[i],"=");
				output = dim[1];
			} else if(args[i].startsWith("-encoding=")){
				String[] dim = Util.split(args[i],"=");
				encoding = dim[1];
			} else if(args[i].startsWith("-showDirectory=")){
				String[] dim = Util.split(args[i],"=");
					showDirectory = dim[1];
			} else {
				fileList.add(new File(args[i]));
			}
		}

		Main main = new Main();
		main.setFiles((File[])fileList.toArray(new File[fileList.size()]));
		main.setFormatter(FormatterFactory.getFormatter(format));
		if(output != null && !output.equals("")){
			main.setOutput(new PrintStream(new FileOutputStream(new File(output))));
		}

		if(encoding != null){
//			encoding = System.getProperty("file.encoding");
			Util.setFileEncoding(encoding);
		}
		if ("true".equalsIgnoreCase(showDirectory)) {
			main.setShowDirectory(true);
		}

		main.executeCount();
	}

}