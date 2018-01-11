public class Kernel {

    private final int[][] kernel;
    public Kernel(int[][] kernel) {
        if(kernel.length == 3 && kernel[1].length == 3) {
            this.kernel = kernel;
        } else {
            throw new IllegalArgumentException("Kernel muss dir Größe 3x3 haben");
        }

    }

}
