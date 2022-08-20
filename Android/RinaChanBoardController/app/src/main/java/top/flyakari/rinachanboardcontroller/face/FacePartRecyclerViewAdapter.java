package top.flyakari.rinachanboardcontroller.face;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import top.flyakari.rinachanboardcontroller.R;

public class FacePartRecyclerViewAdapter extends RecyclerView.Adapter<FacePartRecyclerViewAdapter.ViewHolder> {
    private List<FacePartRecyclerViewItem> mFacePartList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        View facePartView;
        ImageView partImageView;
        TextView partIdTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            facePartView = itemView;
            partImageView = (ImageView) itemView.findViewById(R.id.iv_face_part);
            partIdTextView = (TextView) itemView.findViewById(R.id.tv_face_part_id);
        }
    }

    public FacePartRecyclerViewAdapter(List <FacePartRecyclerViewItem> facePartList){
        mFacePartList = facePartList;
    }

    public interface ItemOnClickCallback{
        void onItemClick(FacePartName facePartName, int facePartId);
    }

    static ItemOnClickCallback mCb;
    public static void setItemOnClickCallback(ItemOnClickCallback cb){
        mCb = cb;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_part_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.facePartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                FacePartRecyclerViewItem facePart = mFacePartList.get(position);
                mCb.onItemClick(facePart.getPartName(), facePart.getId());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FacePartRecyclerViewItem facePart = mFacePartList.get(position);
        holder.partImageView.setImageResource(facePart.getImageId());
        if(facePart.getPartName() == FacePartName.RightEye) {
            holder.partImageView.setScaleX(-1);
        }
        holder.partIdTextView.setText(facePart.getId()+"");
    }

    @Override
    public int getItemCount() {
        return mFacePartList.size();
    }


}
