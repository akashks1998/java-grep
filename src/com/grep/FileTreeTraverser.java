package com.grep;

import com.grep.Executor.ExecutorService;
import com.grep.Executor.ThreadPool;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.regex.Pattern;

class FileTreeTraverser implements Runnable{
    static class Config{
        ArrayList<PathMatcher> subGlobs;
        PathMatcher pathMatcher;
        Pattern pattern;
        ExecutorService ex;
        FileSearcher fs;
        Config(ArrayList<PathMatcher> subGlobs,PathMatcher pathMatcher,Pattern pattern,ExecutorService ex,FileSearcher fs){
            this.subGlobs=subGlobs;
            this.pathMatcher=pathMatcher;
            this.pattern=pattern;
            this.ex=ex;
            this.fs=fs;
        }
    }
    static ExecutorService threadPool=new ThreadPool(5);

    int subGlobIDX;
    Path pwd;
    Config c;
    FileTreeTraverser(int i, Path pwd, Config c){
        subGlobIDX=i;
        this.pwd=pwd;
        this.c=c;
    }
    @Override
    public void run() {
        File f=pwd.toFile();
        if(f.isDirectory()){
            int i;
            for(i=subGlobIDX;i<c.subGlobs.size()&&!c.subGlobs.get(i).matches(pwd);i++){
            }
            if(i!=c.subGlobs.size()){

                for(File path:f.listFiles()){
                    threadPool.submit(new FileTreeTraverser(i, path.toPath(),c));
                }
            }
        }else{
            if (c.pathMatcher.matches(pwd)) {
                c.ex.submit(()->{ c.fs.search(pwd, c.pattern); });
            }
        }
    }
}
