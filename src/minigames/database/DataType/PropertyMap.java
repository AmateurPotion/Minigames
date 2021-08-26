package minigames.database.DataType;

import arc.util.serialization.Jval;

import java.util.HashMap;

public class PropertyMap extends HashMap<String, Object> {
    public final Jval json;

    public PropertyMap() {
        super();
        json = Jval.newObject();
    }

    public PropertyMap(PropertyMap origin) {
        super(origin);
        json = Jval.newObject();

        origin.forEach((key, val) -> {
            if(val instanceof PropertyMap map) json.put(key, Jval.read(map.json.toString()));
            else if(val instanceof Boolean bo) json.put(key, bo);
            else if(val instanceof Number num) json.put(key, num);
            else if(val instanceof String str) json.put(key, str);
        });
    }

    public void putAll(PropertyMap origin) {
        origin.forEach((key, val) -> {
            if(val instanceof PropertyMap map) json.put(key, Jval.read(map.json.toString()));
            else if(val instanceof Boolean bo) json.put(key, bo);
            else if(val instanceof Number num) json.put(key, num);
            else if(val instanceof String str) json.put(key, str);
        });
        super.putAll(origin);
    }

    @Override
    public Object put(String key, Object value) {

        if(value instanceof PropertyMap map) json.put(key, Jval.read(map.json.toString()));
        else if(value instanceof Boolean bo) json.put(key, bo);
        else if(value instanceof Number num) json.put(key, num);
        else if(value instanceof String str) json.put(key, str);

        return super.put(key,value);
    }
}
