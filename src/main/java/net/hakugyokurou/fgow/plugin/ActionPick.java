package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.tasks.Copy;

import com.google.common.io.Files;

public class ActionPick extends ActionMD5 {
	private File inFile;
	
	public ActionPick(File inFile) {
		super();
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
			String originMd5 = md5s.get(detail.getRelativePath().getPathString());
			if(originMd5==null)
				return;
			String nowMd5 = getMD5(file);
			if(nowMd5.equals(originMd5))
				detail.exclude(); //Only copy modified files.
		}
		else if(arg0 instanceof Copy)
		{
			Copy task = (Copy)arg0;
			md5s = new HashMap<String, String>();
			if(started==false)
			{
				started = true;
				try {
					File dest = task.getDestinationDir();
					if(dest.exists())
						FileUtils.deleteDirectory(task.getDestinationDir());
					task.getDestinationDir().mkdir();
				} catch (IOException e1) {
					throw new RuntimeException("Failed to clear directory:"+task.getDestinationDir().getAbsolutePath(), e1);
				}
				if(inFile.exists())
				{
					try {
						List<String> lines = Files.readLines(inFile, Charset.defaultCharset());
						for(String line : lines)
						{
							if(line.trim().isEmpty())
								continue;
							md5s.put(line.substring(0, line.lastIndexOf(':')), 
									line.substring(line.lastIndexOf(':')+1, line.length()-1));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
				{
					throw new RuntimeException("Can't find md5 record:"+inFile.getAbsolutePath());
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
