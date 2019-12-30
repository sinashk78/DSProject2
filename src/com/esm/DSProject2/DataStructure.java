package com.esm.DSProject2;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataStructure {
    private List<BitSet> itemSets;
    private int sizeOfCustomers;

    public DataStructure(){
       this(1);
    }

    public DataStructure(int initialSize){
        itemSets = new ArrayList<>(initialSize);
    }

    public void insert(String products){
        itemSets.add(createBitSet(parseProducts(products)));
        sizeOfCustomers++;
    }


    public float sup(String text){
        long current = System.currentTimeMillis();
        BitSet A = parseForSup(text);
        int count = 0;
        For:
        for (BitSet bitSet : itemSets) {

            for (int i = A.nextSetBit(0); i >= 0; i = A.nextSetBit(i+1)) {
                if(!bitSet.get(i))
                    continue For;
            }
            count++;
        }
        long time = System.currentTimeMillis() - current;
        System.out.println(time);
        return (float) count/sizeOfCustomers;
    }
    public float sup(BitSet subSet){
        int count = 0;
        For:
        for (BitSet bitSet : itemSets) {

            for (int i = subSet.nextSetBit(0); i >= 0; i = subSet.nextSetBit(i+1)) {
                if(!bitSet.get(i))
                    continue For;
            }
            count++;
        }
        return (float) count/sizeOfCustomers;
    }


    public float conf(String text){
        long current = System.currentTimeMillis();
        String[] parsedText = parseForConf(text);
        BitSet A = createBitSet(parseProducts(parsedText[0]));
        BitSet B = createBitSet(parseProducts(parsedText[1]));
        int aCount = 0;
        int bCount = 0;

        For:
        for (BitSet bitSet : itemSets) {

            for (int i = A.nextSetBit(0); i >= 0; i = A.nextSetBit(i+1)) {
                if(!bitSet.get(i))
                    continue For;
            }
            aCount++;
            for (int i = B.nextSetBit(0); i >= 0; i = B.nextSetBit(i+1)) {
                if(!bitSet.get(i))
                    continue For;
            }
            bCount++;
        }
        long time = System.currentTimeMillis() - current;
        System.out.println(time);
        return (float) bCount/aCount;
    }

    public void apriori(float minSup){
        //create the itemSet
        BitSet itemSet = createItemSet(itemSets);
        //create all subsets with 1 element then omit the ones below minSup
        List<BitSet> subSets = new ArrayList<>();
        subset(itemSet , 1 , itemSet.nextSetBit(0) , 0 , new boolean[itemSet.length()] , subSets , minSup);
        int k = 2;
        while (!subSets.isEmpty()){
            print(subSets , minSup);
            itemSet = createItemSet(subSets);
            subSets.clear();
            subset(itemSet , k++ , itemSet.nextSetBit(0) , 0 , new boolean[itemSet.length()] , subSets , minSup);
        }
    }

    private void print(List<BitSet> subSets , float minSup) {
        Iterator<BitSet> iterator = subSets.iterator();
        while (iterator.hasNext())
        {
            BitSet bitSet = iterator.next();
            if(sup(bitSet) > minSup)
            {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
                    stringBuilder.append("A").append(i).append(";");
                }
                stringBuilder.append("}");
                System.out.println(stringBuilder.toString());
            }
            else
                iterator.remove();
        }
    }

    private BitSet createItemSet(List<BitSet> bitSets){
        BitSet itemSet = new BitSet();
        bitSets.forEach(bitSet -> {
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i+1)) {
                itemSet.set(i);
            }
        });
        return itemSet;
    }



    private String[] parseProducts(String products){
        return products
                .replace("A" , "")
                .split(";");
    }

    private BitSet createBitSet(String[] products){
        BitSet bitSet = new BitSet(products.length);
        for (String s : products) {
            bitSet.set(Integer.parseInt(s));
        }
        return bitSet;
    }

    private BitSet parseForSup(String text){
        Pattern pattern = Pattern.compile("A\\d+");
        Matcher matcher = pattern.matcher(text);
        BitSet bitSet = new BitSet();
        while (matcher.find())
            bitSet.set(Integer.parseInt(matcher.group().replace("A" , "")));

        return bitSet;
    }
    private String[] parseForConf(String text){
        Pattern pattern = Pattern.compile("A\\d+(;A\\d+)*");
        Matcher matcher = pattern.matcher(text);
        String[] strings = new String[2];
        int i = 0;
        while (matcher.find())
            strings[i++] = matcher.group();

        return strings;
    }

    public void subset(BitSet A, int k, int start, int currLen, boolean[] used , List<BitSet> subSets , float minSup) {
        if (currLen == k) {
            BitSet bitSet = new BitSet();
            for (int i = A.nextSetBit(0); i >= 0; i = A.nextSetBit(i+1)) {
                if (used[i]) {
                    bitSet.set(i);
                }
            }
            if(sup(bitSet) > minSup)
                subSets.add(bitSet);
            return;
        }
        if (start == A.cardinality()) {
            return;
        }
        if(start == -1)
            return;
        used[start] = true;
        subset(A, k, A.nextSetBit(start + 1), currLen + 1, used,subSets , minSup);
        used[start] = false;
        subset(A, k, A.nextSetBit(start + 1), currLen, used,subSets , minSup);
    }


    public static void main(String[] args) {
        File f = Paths.get("Resources/test2.txt").toFile();
        DataStructure ds = new DataStructure(3);
        try(BufferedReader bfBufferedReader = new BufferedReader(new FileReader(f))){
            String s;
            while ((s = bfBufferedReader.readLine()) != null)
            {
                ds.insert(s);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        long current = System.currentTimeMillis();
        ds.apriori(.3f);
        long time = System.currentTimeMillis() - current;
        System.out.println("execution time: " + time);
    }

    public void fromFile(String path) {
        File f = Paths.get(path).toFile();
        try(BufferedReader bfBufferedReader = new BufferedReader(new FileReader(f))){
            String s;
            while ((s = bfBufferedReader.readLine()) != null)
                this.insert(s);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
