package main.java;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;
import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;

import java.io.*;
import java.util.*;

class TopicModeling {
    static String model_path = "./models";
    static Komoran instance = new Komoran(model_path);
    static String tag_path = "./tag_list.txt";
    static Set<String> tag_set;

    static Properties prop = new Properties();

    public static void main(String[] args)
        throws Exception
    {

        String propertyFilePath = "./main.properties";
        prop.load(new FileInputStream(propertyFilePath));

        // tag loader
        Set<String> tag_list = tag_loader(tag_path);
        System.out.println(komoran(instance, "�븳湲��쓽 �쐞�븳 �뀒�뒪�듃"));
        String origin_path = "./test.txt";
        String result = Komoran_form_transform(origin_path);
        Mallet_topic_modeling(result);

    }

    public static String Komoran_form_transform(String origin_path) throws Exception {


        File data = new File(origin_path);
        BufferedReader data_reader = new BufferedReader(new FileReader(data));
        String[] path_temp = origin_path.split("/");
        String file_name = path_temp[path_temp.length-1];
        File result = new File("./komoran_test.txt");
        BufferedWriter data_writer = new BufferedWriter(new FileWriter(result));

 
        String s = "";
        String dump = "";
        int num = 0;
        while ((s = data_reader.readLine()) != null) {


         
                String analyed_string = komoran(instance, s.trim());
                String line = Integer.toString(num) + "\t" + "X" + "\t" + analyed_string + "\n";
                data_writer.write(line);
                System.out.println(line);
                ++num;
                s = "";
        }

        data_writer.close();
        return "./komoran_test.txt";
    }

    public static Set<String> tag_loader(String path)
        throws Exception
    {
        File file = new File(path);
        tag_set = new HashSet<String>();

        if (file.isFile()) {
            BufferedReader tag = new BufferedReader(new FileReader(file));

            String s = "";
            while ((s = tag.readLine()) != null) {
                tag_set.add(s);
            }
            System.out.println("tag_set :" + tag_set.toString());
        }
        return tag_set;

    }

    public static String komoran(Komoran instance, String text) {
        List<List<Pair<String, String>>> result = instance.analyze(text);
        String analyzed_text = "";
        for (List<Pair<String, String>> eojeolResult : result) {

            for (Pair<String, String> wordMorph : eojeolResult) {
                if (tag_set.contains(wordMorph.getSecond())) {
                    System.out.println(wordMorph.toString());
                    analyzed_text += wordMorph.getFirst().toString().replace(" ", ",") + " ";
                    System.out.println();
                }
            }

        }
        return analyzed_text;

    }

    public static InstanceList Mallet_data_loader(String origin_path, String mallet_data_path)
        throws Exception
    {
        ImportMalletData importer = new ImportMalletData();

        InstanceList instances = importer.readFile(new File(origin_path));
        instances.save(new File(mallet_data_path));
        return instances;

    }

    public static void Mallet_topic_modeling(String komoraned_docu_path)
        throws Exception
    {

        String path2 = "./mallet_test.txt";
        String mallet_result = "./mallet_result";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(mallet_result)));
        InstanceList instances = Mallet_data_loader(komoraned_docu_path, path2);
        int numTopics = Integer.parseInt(prop.getProperty("mallet.numofTopics"));
        
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);
        
        int numThread = Integer.parseInt(prop.getProperty("mallet.thread"));
        model.setNumThreads(numThread);

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        
        int numIteration = Integer.parseInt(prop.getProperty("mallet.numIterations"));
        model.setNumIterations(numIteration);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();

        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.KOREAN);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);

        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;

            }
            writer.write(out.toString() + "\n");
            System.out.println(out);

        }
        writer.close();
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }
        System.out.println(topicZeroText.toString());

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities[0]);

    }

}
