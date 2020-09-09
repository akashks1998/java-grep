package com.grep;

import com.grep.Executor.ExecutorService;
import com.grep.Executor.ThreadPool;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum FileGlobNIO {
    PARALLEL(FileSearcher.PARALLEL),SEQUENTIAL(FileSearcher.SEQUENTIAL);
    FileGlobNIO(FileSearcher fs){
        this.fs=fs;
    }
    ExecutorService ex=new ThreadPool(5);
    FileSearcher fs;

    ArrayList<PathMatcher> getSubGlob(String glob){
        boolean bracket=false;
        boolean escaping=false;
        ArrayList<PathMatcher> res=new ArrayList<>();
        for(int i=0;i<glob.length();i++){
            if(glob.charAt(i)=='\\') {
                escaping=!escaping;
            }else{
                if(glob.charAt(i)=='['&&!escaping){
                    bracket=true;
                }
                if(glob.charAt(i)==']'&&!escaping)
                    bracket=false;
                if(glob.charAt(i)=='/'&&!bracket&&!escaping){
                    res.add(FileSystems.getDefault().getPathMatcher(glob.substring(0,i)));
                }
                escaping=false;
            }
        }
        res.add(FileSystems.getDefault().getPathMatcher(glob));
        return res;
    }
    public void match(String glob, Pattern pattern) {

        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
                glob);
        final ArrayList<PathMatcher> dirMatchers=getSubGlob(glob);
        FileTreeTraverser ft=new FileTreeTraverser(0,Paths.get("/Users"),new FileTreeTraverser.Config(dirMatchers,pathMatcher,pattern,ex,fs));
        FileTreeTraverser.threadPool.submit(ft);
    }
    public void end() throws InterruptedException {
        FileTreeTraverser.threadPool.kill();
        FileTreeTraverser.threadPool=new ThreadPool(5);
        ex.kill();
    }
}
