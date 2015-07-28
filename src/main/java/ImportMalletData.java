package main.java;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;


/**
 * Created by seunghyun on 2015-05-10.
 */


public class ImportMalletData{

    Pipe pipe;

    public ImportMalletData() {
        pipe = buildPipe();
    }

    public Pipe buildPipe() {
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers,
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
                Pattern.compile("\\S+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        //pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        //pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field:
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
    //    pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        //pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
        pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

    public InstanceList readFile(File data) throws Exception {

        // Construct a file iterator, starting with the
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the
        //   filename to produce a class label. In this case, I've
        //   asked it to use the last directory name in the path.
//        FileIterator iterator =
//                new FileIterator(directories,
//                        new TxtFilter(),
//                        FileIterator.LAST_DIRECTORY);

        CsvIterator iterator = new CsvIterator(new FileReader(data),
                "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$",
                3, 2, 1); // (data, target, name) field indices


        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }


    public static void main (String[] args) throws Exception {

        ImportMalletData importer = new ImportMalletData();
        String path = "src/main/resources/text/mallet_MLB_cont_2015.csv";
        String path2 = "src/main/resources/topic_data/result";
        InstanceList instances = importer.readFile(new File(path));
        instances.save(new File(path2));

    }

    /** This class illustrates how to build a simple file filter */
    class TxtFilter implements FileFilter {

        /** Test whether the string representation of the file
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        public boolean accept(File file) {
            return file.toString().endsWith(".csv");
        }
    }

}