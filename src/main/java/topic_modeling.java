/**
 * Created by seunghyun on 2015-05-06.
 */


import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;

import javax.xml.transform.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class topic_modeling {
    static String model_path = "src/main/resources/models";
    static Komoran instance = new Komoran(model_path);

    public static void main(String[] args) throws Exception {



/*
        String text_path ="src/main/resources/text";
        try {
            System.out.println((String) new File(text_path).listFiles().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
*/


        System.out.println(komoran(instance, "한글의 위한 테스트"));

        // tag loader

        List<String> tag_list = tag_loader("src/main/resource/tag.txt");
        List<String> docu_list = Mallet_form_transform("src/main/resource");


    }

    public static List<String> tag_loader(String path) throws Exception {
        BufferedReader tag = new BufferedReader(new FileReader(path));
        List<String> tag_list = new ArrayList<String>();
        String s = "";
        while ((s = tag.readLine()) != null) {
            tag_list.add(s);
        }
        return tag_list;

    }

    /*
    pass the folder path as a parameter
     */
    public static List<String> Mallet_form_transform(String path) throws Exception {
        int docu_num = 0;
        File[] _file_list = new File(path).listFiles();
        List<String> docu_list = new ArrayList<String>();
        for (File name : _file_list) {
            String s = "";
            while ((s = new BufferedReader(new FileReader(name)).readLine()) != null) {
                docu_list.add(String.format("%d X %s", docu_num, komoran(instance, s)));
                ++docu_num;
            }
            ;

        }
        return docu_list;

    }

    public static String komoran(Komoran instance, String text) {
        List<List<Pair<String, String>>> result = instance.analyze(text);
        System.out.println("");
        String analyzed_text = "";
        for (List<Pair<String, String>> eojeolResult : result) {

            for (Pair<String, String> wordMorph : eojeolResult) {
                System.out.println(wordMorph.toString());
                analyzed_text += wordMorph.getFirst().toString() + " ";


            }
            System.out.println();
        }
        return analyzed_text;


    }


}


