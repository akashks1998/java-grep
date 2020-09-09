package com.grep;

//import org.jetbrains.annotations.NotNull;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import javafx.util.Pair;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

enum FileSearcher {
    PARALLEL{
        void search(Path path, Pattern pattern){
            try(Stream<String> lines = Files.lines(path)) {
                lines.parallel().map(s->new Pair<>(s,pattern.matcher(s)))
                        .filter(s->s.getValue().find())
                        .forEach(s -> System.out.println(path+":"+getPrintingString(s)));
            }catch(Exception ex){
                System.out.println("\u001B[31m File :" +path +"showed error \u001B[0m");
            }
        }
    },SEQUENTIAL{
        void search(Path path, Pattern pattern){
            try {
                String content = new String ( Files.readAllBytes(path) );
                Matcher m=pattern.matcher(content);
                while(m.find()){
                    System.out.println(path+": "+content.substring(content.lastIndexOf('\n',m.start()),m.start())+"\u001B[31m"+m.group()+"\u001B[0m"+content.substring(m.end(),content.indexOf('\n',m.end())));
                }

            }catch(Exception ex){
                System.out.println("\u001B[31m File :" +path +"showed error \u001B[0m");
            }
        }
    };
    String getPrintingString(Pair<String,Matcher> s){
        int start=s.getValue().start();
        String res=s.getKey().substring(0,start)+"\u001B[31m"+s.getValue().group()+"\u001B[0m";
        int end=s.getValue().end();
        while(s.getValue().find()){
            start=s.getValue().start();

            res+=s.getKey().substring(end,start)+"\u001B[31m"+s.getValue().group()+"\u001B[0m";
            end=s.getValue().end();
        }
        return res+s.getKey().substring(end);
    }
    abstract void search(Path path, Pattern pattern);
}
