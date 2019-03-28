
public class NewClass {
    public static void main(String [] args){
            int n = 10;
    int i,j;
int[][] matrix = new int[n][n];
    for( i = 0; i < n; i++){
        for ( j = 0; j < n; j++) {
            int i2 = (int)Math.abs(i-n/2f + 0.5f);
            int j2 = (int)Math.abs(j-n/2f + 0.5f);
            matrix[i][j] = 1 + (i2>j2?i2:j2);
            System.out.print(matrix[i][j]+" ");
        }
        System.out.println("");
    }
    }
}
