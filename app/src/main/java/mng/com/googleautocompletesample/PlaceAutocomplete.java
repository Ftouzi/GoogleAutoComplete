package mng.com.googleautocompletesample;

/**
 * Created by Farouk Touzi.
 */
public class PlaceAutocomplete {

    public CharSequence placeId;
    public CharSequence description;

    PlaceAutocomplete(CharSequence placeId, CharSequence description) {
        this.placeId = placeId;
        this.description = description;
    }

    public CharSequence getPlaceId() {
        return placeId;
    }

    public void setPlaceId(CharSequence placeId) {
        this.placeId = placeId;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description.toString();
    }

}
