package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.tasks.AbstractCopyTask;

import com.google.common.io.Files;

class ActionCheckMD5Record extends ActionMD5 {
	private File sourceFolder;
	private File[] inFile;
	
	public ActionCheckMD5Record(File sourceFolder, File ... inFile) {
		super();
		this.sourceFolder = sourceFolder;
		this.inFile = inFile;
	}
	
	@Override
	public void execute(Object arg0) {
		if(arg0 instanceof FileCopyDetails)
		{
			FileCopyDetails detail = (FileCopyDetails)arg0;
			File file = detail.getFile();
			if(!file.isFile())
				return;
			if(md5s==null) //If there is not md5record.buildigrone, just copy normally.
				return;
			String originMd5 = null;
			if(file.getName().endsWith(".class"))
			{
				String originName = detail.getRelativePath().getPathString().replace(".class", ".java");
				if(originName.contains("$"))
				{
					originName = originName.substring(0, originName.indexOf('$')) + ".java";
				}
				originMd5 = md5s.get(originName);
				file = new File(sourceFolder, originName);
				//System.out.println("Class:"+detail.getRelativePath().getPathString()+" Origin:"+originName);
			}
			else
			{
				originMd5 = md5s.get(detail.getRelativePath().getPathString());
			}
			if(originMd5==null || !file.exists())
				return;
			String nowMd5 = getMD5(file);
			//System.out.println(file.getName()+" - nowMD5:"+nowMd5+" originMD5:"+originMd5);
			if(nowMd5.equals(originMd5))
				detail.exclude(); //Our lower deck was torn up. Abandon ship now!
		}
		else if(arg0 instanceof AbstractCopyTask)
		{
			if(started==false)
			{
				started = true;
				if(inFile==null || inFile.length==0)
				{
					md5s = null;
					return;
				}
				md5s = new HashMap<String, String>();
				for(File file : inFile)
				{
					if(file.exists())
					{
						try {
							List<String> lines = Files.readLines(file, Charset.defaultCharset());
							for(String line : lines)
							{
								if(line.trim().isEmpty())
									continue;
								md5s.put(line.substring(0, line.lastIndexOf(':')), 
										line.substring(line.lastIndexOf(':')+1, line.length()-1));
								//throw new RuntimeException(line.substring(0, line.lastIndexOf(':')-1)+
								//		":"+line.substring(line.lastIndexOf(':')+1, line.length()-2));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			else
			{
				started = false;
				md5s.clear();
			}
		}
		else 
		{
			throw new RuntimeException("Unexpected arg when copying.");
		}
	}
}
