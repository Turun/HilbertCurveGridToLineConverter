
/**
 * Class to map a input "line" to a 2D grid and the otherway around, using the spacefilling hilbert curve
 * 
 * this video got me thinking about it: https://www.youtu.be/3s7h2MHQtxc
 * 
 * made by Turun Ambartenen
 */
public class HilbertCurveConverter
{
    
    
    /**
     * map an Array (only works if square with sidelength 2^x) to a line with a hilbert curve.
     * 
     * example curve for a 2x2 grid (curve/line start is on the bottom left)
     *  _
     * | |
     * 
     * example construction of a curve for a 4x4 grid with the pieces from the 2x2 grid (curve/line start is on the bottom left)
     *  _   _
     * | | | |
     *  _   _
     * | | | |
     *
     *--------------
     *  _   _
     * | | | |
     *  _   _
     *  _| |_
     *
     *--------------
     *  _   _
     * | |_| |
     * |_   _|
     *  _| |_
     *  
     *--------------
     */
    public static Object[] mapGridToLine(Object[][] in){
        //input is null
        if(in == null){return null;}
        
        //input array x dimension is not power of two (neccessary for hilbert curve)
        if(!isPowOfTwo(in.length)){return null;}
        
        //nested length is power of two and all nested arrays are of equal lenght
        int nestedLength = in[0].length;
        if(!isPowOfTwo(nestedLength)){return null;}
        for(Object[] obj : in){
            if(obj.length != nestedLength){return null;}
        }
        
        
        /** input is a square array, conversion can begin **/
        return mapToLine(in);
    }
    
    /**
     * method to recursively map the (valid) input grid to a line 
     */
    public static Object[] mapToLine(Object[][] in){ 
        //couldn't get it to work with recursion below 2x2 level, so it's hardcoded.
        if(in.length == 2){return new Object[] {in[1][0], in[0][0], in[0][1], in[1][1]};}
        
        //this should never happen, because in.length == 2 is true before this is reached by recursion. 
        //Only way would be to feed a 1x1 2d array. If this happens, return the only byte that is in this array, one byte is already sorted  
        if(in.length == 1){return new Object[] {in[0][0]};}
        
        //just some constants to make it easier to read the loops
        final int len = in.length;
        final int hLen = len>>1;
        
        //now the array is subdivided into four smaller 2DArrays, which then sort themselfes out with this method (recursively)
        //the sorted/line forms are stored in byte[], topLeft, topRight, bottomLeft and bottomRight
        
        //create an array 1/4 the size and copy the corresponding data in it.
        Object[][] tl = new Object[hLen][hLen];
        for(int y = 0; y < hLen; y++){
            for(int x = 0; x < hLen; x++){
                tl[y][x] = in[y][x];
            }
        }
        //get that array in line form
        Object[] tlSol = mapToLine(tl);
        
        
        //simple copie for top right
        Object[][] tr = new Object[hLen][hLen];
        for(int y = 0; y < hLen; y++){
            for(int x = 0; x < hLen; x++){
                tr[y][x] = in[y][x+hLen];
            }
        }
        Object[] trSol = mapToLine(tr);
        
        
        //simple copie for bottom left
        Object[][] bl = new Object[hLen][hLen];
        for(int y = 0; y<hLen; y++){
            for(int x = 0; x<hLen; x++){
                bl[y][x]= in[y+hLen][x];
            }
        }
        //This part has to be rotated around its bottom-left to top-right axis
        flipAroundAxisBottomLeftToTopRight(bl);
        Object[] blSol = mapToLine(bl);
        
        
        //simple copie for bottom right
        Object[][] br = new Object[hLen][hLen];
        for(int y = 0; y<hLen; y++){
            for(int x = 0; x<hLen; x++){
                br[y][x] = in[y+hLen][x+hLen];
            }
        }
        //This part has to be rotated around its top-left to bottom-right axis
        flipAroundAxisTopLeftToBottomRight(br);
        Object[] brSol = mapToLine(br);
        
        
        //the line forms now have to be merged into one big array 
        
        Object[] re = new Object[len*len]; //the array that will be returned
        int p = 0; //pointer
        final int part = re.length>>2; //one fourth of the length of the return array. easier reading
        for(; p<part; p++){
            re[p] = blSol[p];
        }
        for(;p<part*2; p++){
            re[p] = tlSol[p-part];
        }
        for(;p<part*3; p++){
            re[p] = trSol[p-(part*2)];
        }
        for(;p<part*4; p++){
            re[p] = brSol[p-(part*3)];
        }
        return re;
    }
    
    
    
    
    
    /**
     * Map a line (length has to be power of four) to a Grid, according to the Hilbert curve
     * example curve for a 2x2 grid (curve/line start is on the bottom left)
     *  _
     * | |
     * 
     * example construction of a curve for a 4x4 grid with the pieces from the 2x2 grid (curve/line start is on the bottom left)
     *  _   _
     * | | | |
     *  _   _
     * | | | |
     *
     *--------------
     *  _   _
     * | | | |
     *  _   _
     *  _| |_
     *
     *--------------
     *  _   _
     * | |_| |
     * |_   _|
     *  _| |_
     *  
     *--------------
     */
    public static Object[][] mapLineToGrid(Object[] in){
        if(in == null){return null;}
        if(!isPowOfFour(in.length)){return null;}

        return mapToGrid(in);
    }
    
    /**
     * method to (recursively) map the line to a grid
     */
    public static Object[][] mapToGrid(Object[] in){
        //if line length is four, just return the right values. 
        if(in.length == 4){return new Object[][] {{in[1], in[2]}, {in[0], in[3]}};}
        //should not happen, but just in case, if you have a single value, put it in a grid and return it.
        if(in.length == 1){return new Object[][] {{in[0]}};}
        
        //variables
        final int len = in.length>>2; //easier to read for-loops
        int p = 0; //pointer for in-array
        
        //copie the first fourth of the input (bottom left) to a new array and let it recursively fix itself.
        Object[] bl = new Object[len];
        for(; p<len; p++){
            bl[p] = in[p];
        }
        //map the line to a grid (recursively). 
        Object[][] blSol = mapToGrid(bl);
        
        //do the same with the second fourth (top left)
        Object[] tl = new Object[len];
        for(; p<len*2; p++){
            tl[p-len] = in[p];
        }
        Object[][] tlSol = mapToGrid(tl);
        
        //do the same with the third fourth (top right)
        Object[] tr = new Object[len];
        for(;p<len*3; p++){
            tr[p-(len*2)] = in[p];
        }
        Object[][] trSol = mapToGrid(tr);
        
        //do the same with the last fourth (bottom right)
        Object[] br = new Object[len];
        for(;p<len*4; p++){
            br[p-(len*3)] = in[p];
        }
        Object[][] brSol = mapToGrid(br);
        
        //the two grids on the bottom have to be flipped to form a hilbert curve.
        flipAroundAxisBottomLeftToTopRight(blSol);
        flipAroundAxisTopLeftToBottomRight(brSol);
        
        //constants for readability
        final int size = root(in.length);
        final int half = size>>1;
        
        //put the four smaller arrays in a big one to return it.
        Object[][] re = new Object[size][size];
        for(int y = 0; y<half; y++){
            for(int x = 0; x<half; x++){
                re[y][x] = tlSol[y][x];
                re[y+half][x] = blSol[y][x];
                re[y][x+half] = trSol[y][x];
                re[y+half][x+half] = brSol[y][x];
            }
        }
        return re;
    }
    
    
    
    
    
    
    
    
    /**
     * flips the given byte[][] around the top-left to bottom-right axis. e.g the value at [1][0] gets mapped to [0][1]
     */
    public static void flipAroundAxisTopLeftToBottomRight(Object[][] in){
        for(int y = 0; y<in.length; y++){
            for(int x = y+1; x<in[y].length; x++){
                Object temp = in[y][x];
                in[y][x] = in[x][y];
                in[x][y] = temp;
            }
        }
    }
    
    /**
     * flips the given byte[][] around the bottom-left to top-right axis. e.g the value at [0][0] gets mapped to [-1][-1]
     */
    public static void flipAroundAxisBottomLeftToTopRight(Object[][] in){
        for(int y = 0; y<in.length; y++){
            for(int x = 0; x<in.length-y-1; x++){
                Object temp = in[y][x];
                in[y][x] = in[in.length-1-x][in.length-1-y];
                in[in.length-1-x][in.length-1-y] = temp;
            }
        }
    }
    
    /**
     * checks if input i is a power of 2
     */
    public static boolean isPowOfTwo(int i){
        for(int a = 1; a<Integer.MAX_VALUE; a = a<<1){
            if(a == i){return true;}
        }
        return false;
    }
    
    /**
     * checks if input is power of 4
     */
    public static boolean isPowOfFour(int i){
        for(int a = 1; a<Integer.MAX_VALUE; a = a<<2){
            if(a == i){return true;}
        }
        return false;
    }
    
    /**
     * returns the root of the given power of four
     */
    public static int root(int in){
        int pow = 0;
        for(int i = 1;i<Integer.MAX_VALUE; i = i<<2 , pow++){
            if(in == i){break;}
        }
        return in>>pow;
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * for testing the Methods (prints on Console)
     */
    public static void test(){
        Byte[] oneDimensional = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 
            'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-'};
        
            
        Byte[][] twoDimensional = {
            {'v', 'w', 'z', 'A', 'L', 'M', 'P', 'Q'},
            {'u', 'x', 'y', 'B', 'K', 'N', 'O', 'R'},
            {'t', 's', 'D', 'C', 'J', 'I', 'T', 'S'},
            {'q', 'r', 'E', 'F', 'G', 'H', 'U', 'V'},
            {'p', 'm', 'l', 'k', '1', '0', 'Z', 'W'},
            {'o', 'n', 'i', 'j', '2', '3', 'Y', 'X'},
            {'b', 'c', 'h', 'g', '5', '4', '9', '+'},
            {'a', 'd', 'e', 'f', '6', '7', '8', '-'},};
        
        for(Object b : mapGridToLine(mapLineToGrid((Object[])oneDimensional))){
            System.out.println((char)(byte)b);
        }
        System.out.println("\n");
        for(Object b : mapGridToLine((Object[][])twoDimensional)){
            System.out.println((char)(byte)b);
        }
    }
    
    
    
    
}





