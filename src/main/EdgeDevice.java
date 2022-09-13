package main;

import Detector.Detector;
import Detector.NewNETS;
import RPC.RPCFrame;
import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.HashFamily;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

@SuppressWarnings("unchecked")
public class EdgeDevice extends RPCFrame implements Runnable {
    public ArrayList<Vector> rawData;
    public ArrayList<Vector> allRawDataList = new ArrayList<>();

    private final int numberOfHashTables;
    public Index index;
    public HashSet<Integer> aggFingerprints;

    public EdgeNode nearestNode;

    public Detector detector;

    public EdgeDevice(Index index, int NumberOfHashes, int NumberOfHashTables){
        this.port = new Random().nextInt(50000)+10000;
        this.detector = new NewNETS(0);
        this.numberOfHashTables = NumberOfHashTables;
        this.index = index;
        this.aggFingerprints = new HashSet<Integer>();
    }
    public void clearFingerprints(){
        this.aggFingerprints = new HashSet<Integer>();
    }
    public List<Vector> detectOutlier(long currentTime) throws Throwable {
//        System.out.println(Thread.currentThread().getName()+" "+this+": receive data and detect outlier: "+this.rawData.size());
        generateAggFingerprints(rawData);
        sendAggFingerprints();
        System.out.println("zxy: "+this.allRawDataList.size());
//        detector.detectOutlier(allRawDataList,currentTime);
        return rawData;
    }

    public void generateAggFingerprints(List<Vector> data){
//        System.out.println("raw data size: "+data.size());
        for (Vector datum : data) {
            for (int j = 0; j < this.numberOfHashTables; j++) {
                int bucketId = index.getHashTable().get(j).getHashValue(datum);
//                System.out.println(Thread.currentThread().getName()+": "+bucketId);
                aggFingerprints.add(bucketId);
            }
        }
//        for (int j = 0; j < this.numberOfHashTables; j++) {
//            System.out.println(Thread.currentThread().getName()+"agg size: "+j+" "+aggFingerprints[j].size());
//        }
//        System.out.println(Thread.currentThread().getName()+": "+this+"generateAggFingerprints "+ aggFingerprints.length);
    }

    public void sendAggFingerprints() throws Throwable {
//        System.out.println(Thread.currentThread().getName()+": "+this+" sendAggFingerprints, invoke upload");
        Object[] parameters = new Object[]{aggFingerprints};
        List<Vector> result = (List<Vector>) invoke("localhost",this.nearestNode.port,EdgeNode.class.getMethod("upload", HashSet.class),parameters);
        if(!result.isEmpty()){
            this.allRawDataList.addAll(result);
        }
        this.allRawDataList.addAll(this.rawData);
    }

    public List<Vector> sendData(){
        return rawData;
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }

    public void setRawData(ArrayList<Vector> rawData) {
        this.rawData = rawData;
    }
}
