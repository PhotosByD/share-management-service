package si.photos.by.d.share.models.entities;

import javax.persistence.*;

import javax.persistence.*;
import java.util.List;

@Entity(name="share")
@NamedQueries(value =
        {
                @NamedQuery(name = "Share.getAll", query = "SELECT a FROM share a")
        })
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "photo_id")
    private Integer photoId;

    @Column(name = "user_id")
    private Integer userId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Integer photoId) {
        this.photoId = photoId;
    }

}