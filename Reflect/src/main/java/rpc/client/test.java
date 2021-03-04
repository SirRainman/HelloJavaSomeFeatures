package rpc.client;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-03-04 19:23
 **/
public class test {
    public static void main(String[] args) {
        test test = new test();
        System.out.println(test.a(6));
    }

    public int a(int n) {
        boolean[] nums = new boolean[n];
        int cur = n, i = 2;
        while(cur > 1){
            nums[i] = true;
            int j = i, cnt = 0;
            while(cnt < 3){
                j = (j + 1) % n;
                if(!nums[j]) cnt++;
            }
            i = j;
            cur--;
        }
        System.out.println("haha");
        for(int j = 0; j < n; j++)
            if(!nums[j]) return j + 1;
        return 1;
    }
}
