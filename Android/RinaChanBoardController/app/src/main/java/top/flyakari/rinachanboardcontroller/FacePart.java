package top.flyakari.rinachanboardcontroller;
enum FacePartName{
    LeftEye, RightEye, Cheek, Mouth;
}
public class FacePart {
    private int imageId;
    private int id;
    private FacePartName partName;

    public FacePart(FacePartName partName, int imageId, int id){
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
