package com.nilesh.cym.temp;

import java.util.HashMap;
import java.util.Map;

class Solution {

    public static void main(String[] args) {
        System.out.println(trap(new int[] {4,2,0,3,2,5}));
    }

    public static int trap(int[] height) {
        Map<Integer, Integer> map = new HashMap<>();

        int i=0, j=0, n = height.length, ans =0, sum =0;

        while(height[i]==0) i++;
        j = i;

        while(j<n){
            sum+= height[j];
            int dis = j-i-1;


            if(height[j]==0){
                map.put(j, sum);
                j++;
                continue;
            }

            else if(j != 0 && height[j] < height[j-1] && height[j-1] >= height[i]){
                i = j-1;
            }

            else {
                if(i!=j && height[i] < height[j]){
                    int pre = map.get(j-1) - map.get(i);

                    ans += height[i] * dis - pre;
                }
                else if(i!=j){
                    int pre = map.get(j-1) - map.get(i);

                    ans += height[j] * dis - pre;
                }
            }

            map.put(j, sum);
            j++;
        }
        return ans;
    }
}