package example.fussen.mapdemo.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.services.core.LatLonPoint;

/**
 * Created by Fussen on 2016/11/1.
 *
 * 每个条目中的信息
 */

public class SearchAddressInfo implements Parcelable {

    public String addressName;
    public LatLonPoint latLonPoint;
    public boolean isChoose;
    public String title;

    public SearchAddressInfo(String title, String addressName, boolean isChoose, LatLonPoint latLonPoint) {
        this.addressName = addressName;
        this.latLonPoint = latLonPoint;
        this.isChoose = isChoose;
        this.title = title;
    }

    protected SearchAddressInfo(Parcel in) {
        addressName = in.readString();
        latLonPoint = in.readParcelable(LatLonPoint.class.getClassLoader());
        isChoose = in.readByte() != 0;
        title = in.readString();
    }

    public static final Creator<SearchAddressInfo> CREATOR = new Creator<SearchAddressInfo>() {
        @Override
        public SearchAddressInfo createFromParcel(Parcel in) {
            return new SearchAddressInfo(in);
        }

        @Override
        public SearchAddressInfo[] newArray(int size) {
            return new SearchAddressInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addressName);
        dest.writeParcelable(latLonPoint, flags);
        dest.writeByte((byte) (isChoose ? 1 : 0));
        dest.writeString(title);
    }
}
