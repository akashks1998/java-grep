package com.grep;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Grep {
    final Pattern pattern;
    final ArrayList<String> dirs;
    FileGlobNIO traverser;
    static class builder{
        String pattern;
        ArrayList<String> dirs;
        boolean caseInsenstive=false, recursive=false,parallel=false;
        builder(){
            dirs=new ArrayList<>();
        }
        void flagHandler(String s){
            if(s.charAt(0)!='-'){
                throw new InvalidParameterException("Incorrect use of flagHandler");
            }
            s=s.substring(1);
            for(char c:s.toCharArray()){

                if(c=='R')isRecursive();
                else if(c=='i')isCaseInsenstive();
                else if(c=='p')isParallel();
                else throw new InvalidParameterException("No such flag exist");
            }
        }
        void setPattern(String p){
            pattern=p;
        }
        void isCaseInsenstive(){
            caseInsenstive=true;
        }
        void isParallel(){
            parallel=true;
        }
        void isRecursive(){
            recursive=true;
        }
        void addDirs(String s) throws IOException {
            dirs.add("glob:"+new File(s).getCanonicalPath());
        }
        Grep build() throws ConfigurationException {

            Pattern p;
            ArrayList<String> d=new ArrayList<>();
            FileGlobNIO traverser;
            if(pattern==null){
                throw new ConfigurationException("Pattern Not Set");
            }
            if(caseInsenstive){
                p=Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
            }else{
                p=Pattern.compile(pattern);
            }
            if(dirs.size()==0){
                throw new ConfigurationException("Directories Not provided");
            }
            if(recursive){
                for(String s:dirs){
                    d.add(s+"/**");
                }
            }else{
                for(String s:dirs){
                    d=dirs;
                }
            }
            if(parallel){
                traverser=FileGlobNIO.PARALLEL;
            }else{
                traverser=FileGlobNIO.SEQUENTIAL;
            }
            return new Grep(p,d,traverser);
        }
    }
    Grep(Pattern pattern, ArrayList<String> dirs, FileGlobNIO traverser){
        this.pattern=pattern;
        this.dirs=dirs;

        this.traverser=traverser;
    }
    void search() throws IOException, InterruptedException {
        for(String dir:dirs){
            traverser.match(dir,pattern);
        }
        traverser.end();
    }
    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        builder b=new builder();
        int i=0;
        for(;i<args.length && args[i].charAt(0)=='-';i++){
            b.flagHandler(args[i]);
        }
        b.setPattern(args[i]);
        i++;
        for(;i<args.length;i++){
            b.addDirs(args[i]);
        }
        Grep g=b.build();
        long start = System.currentTimeMillis();
        g.search();
        System.out.println("Time taken: "+(System.currentTimeMillis()-start));
    }
}
