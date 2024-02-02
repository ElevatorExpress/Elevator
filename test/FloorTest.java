import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FloorTest {

    static Floor f;

    @BeforeAll
    static void createFloor(){
        f = new Floor(new UtilityInterface<>() {
            @Override
            public Floor.FloorInfo get() {
                return null;
            }

            @Override
            public Floor.FloorInfo put(Floor.FloorInfo floorInfo) {
                return null;
            }
        });
    }

    @Test
    void get() {
        Assertions.assertNull(f.get());
    }

    @Test
    void put() {
        Object fInfo = f.put(new Floor.FloorInfo());
        Assertions.assertNotNull(fInfo);
    }
}