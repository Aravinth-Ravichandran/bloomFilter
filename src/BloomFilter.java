import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.zip.CRC32;
import java.util.zip.Adler32;

public class BloomFilter {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numHashFunctions;

    public BloomFilter(int capacity, int numHashFunctions) {
        this.bitSetSize = capacity;
        this.numHashFunctions = numHashFunctions;
        this.bitSet = new BitSet(bitSetSize);
    }

    public void add(String element) {
        int[] hashCodes = getHashCodes(element);
        for (int hash : hashCodes) {
            bitSet.set(Math.abs(hash) % bitSetSize);
        }
    }

    public boolean mightContain(String element) {
        int[] hashCodes = getHashCodes(element);
        for (int hash : hashCodes) {
            if (!bitSet.get(Math.abs(hash) % bitSetSize)) {
                return false;
            }
        }
        return true;
    }

    private int[] getHashCodes(String element) {
        int[] hashCodes = new int[numHashFunctions];
        hashCodes[0] = element.hashCode();

        // Using additional hash functions
        CRC32 crc = new CRC32();
        crc.update(element.getBytes(StandardCharsets.UTF_8));
        hashCodes[1] = (int) crc.getValue();

        if (numHashFunctions > 2) {
            Adler32 adler = new Adler32();
            adler.update(element.getBytes(StandardCharsets.UTF_8));
            hashCodes[2] = (int) adler.getValue();
        }

        // Generate remaining hash codes by mixing hash values
        for (int i = 3; i < numHashFunctions; i++) {
            hashCodes[i] = hashCodes[i - 1] * 31 + hashCodes[0];
        }
        return hashCodes;
    }

    public static void main(String[] args) {
        BloomFilter bloomFilter = new BloomFilter(1000, 3);

        // Adding elements
        bloomFilter.add("hello");
        bloomFilter.add("world");

        // Checking elements
        System.out.println(bloomFilter.mightContain("hello")); // likely true
        System.out.println(bloomFilter.mightContain("world")); // likely true
        System.out.println(bloomFilter.mightContain("goodbye")); // likely false
    }
}
