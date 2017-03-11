package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@MappedSuperclass
public class Audit extends Model{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @CreatedTimestamp
    private Timestamp whenCreated;

    @UpdatedTimestamp
    private Timestamp whenUpdated;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getWhenCreated() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(whenCreated);
    }

    public String getWhenUpdated() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(whenUpdated);
    }

}
