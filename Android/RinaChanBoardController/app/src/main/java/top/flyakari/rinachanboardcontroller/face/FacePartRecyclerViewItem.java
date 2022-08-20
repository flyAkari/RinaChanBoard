package top.flyakari.rinachanboardcontroller.face;

public class FacePartRecyclerViewItem {
    private int imageId;
    private int id;
    private FacePartName partName;

    public FacePartRecyclerViewItem(FacePartName partName, int imageId, int id){
        this.partName = partName;
        this.imageId = imageId;
        this.id = id;
    }

    public int getImageId(){
        return imageId;
    }

    public int getId(){
        return id;
    }

    public FacePartName getPartName(){
        return partName;
    }
}
