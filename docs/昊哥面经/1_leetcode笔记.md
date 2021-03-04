# 没有AC的原因

1精度问题

int a= 10000;int b= 10000;int c= 10000;

long d;

d = a * b * c;会溢出，abc全都要用long

2 BFS起点visited问题

# leetcode179 最大数

public int compare(String a, String b) {
            String order1 = a + b;
            String order2 = b + a;
           return order2.compareTo(order1);
 }



# 前缀树

```java
class Trie {
	class TireNode{
		private final int R = 26;
		private TireNode[] nodelist;
		private boolean isend;
		public TireNode() {
			nodelist = new TireNode[R];		
		}
		public void insert(char c, TireNode node) {
			nodelist[c - 'a'] = node;
		}
		public void setend() {
			this.isend = true;
		}
		public boolean contains(char c) {
			return nodelist[c - 'a'] != null;
		}
		public TireNode geTireNode(char c) {
			return nodelist[c - 'a'];
		}
		public boolean getend() {
			return this.isend;
		}
	}
	TireNode root;
    /** Initialize your data structure here. */
    public Trie() {
        root = new TireNode();
    }   
    /** Inserts a word into the trie. */
    public void insert(String word) {
        TireNode t = root;
        for(char c : word.toCharArray()) {
        	if(t.contains(c)) {
        		t = t.geTireNode(c);
        	}else {
				t.insert(c, new TireNode());
				t = t.geTireNode(c);
			}
        }
        t.setend();
    }
    
    /** Returns if the word is in the trie. */
    public boolean search(String word) {
        TireNode t = root;
        for(char c : word.toCharArray()) {
        	if(t.contains(c)) {
        		t = t.geTireNode(c);
        	}else {
				return false;
			}
        }
        return t.getend();
    }
    
    /** Returns if there is any word in the trie that starts with the given prefix. */
    public boolean startsWith(String prefix) {
    	TireNode t = root;
        for(char c : prefix.toCharArray()) {
        	if(t.contains(c)) {
        		t = t.geTireNode(c);
        	}else {
				return false;
			}
        }
        return true;
    }
}

```

# 二叉树

## 序列化二叉树

```
    1
   / \
  2   3
     / \
    4   5

序列化为 "[1,2,3,null,null,4,5]"
```

```java
class Codec {
    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(root);
        while (!queue.isEmpty()) {
			int size = queue.size();
			while (size -- > 0) {
				TreeNode t = queue.poll();
				if(t == null) {
					sb.append("null,");
				}else {
					sb.append(t.val).append(",");
					queue.offer(t.left);
					queue.offer(t.right);					
				}
			}
		}
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        return sb.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
    	if(data.equals("[null]") || data == null) {
    		return null;
    	}
        String[] str = data.substring(1, data.length() - 1).split(",");
        TreeNode t = new TreeNode(Integer.valueOf(str[0]));
        TreeNode root = t;
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.offer(t);
        int index = 1;
        while(!queue.isEmpty()) {
        	int size = queue.size();
        	while(size -- > 0) {
        		t = queue.poll();
        		if(str[index].equals("null")) {
        			t.left = null;
        		}else {
					t.left = new TreeNode(Integer.valueOf(str[index]));
					queue.offer(t.left);
				}
        		index++;
        		if(str[index].equals("null")) {
        			t.right = null;
        		}else {
					t.right = new TreeNode(Integer.valueOf(str[index]));
					queue.offer(t.right);
				}
        		index++;
        	}
        }
        return root;
    }
}
```

## morris遍历

leetcode99

morris中序

1， 若当前节点左孩子不为空，找到左孩子最右节点。如果最右节点右指针指向当前节点，那么将其置为空，访问当前节点，然后进入左孩子。否则，将最右节点右指针指向当前节点，然后进入右孩子。

2， 如果当前节点的左孩子为空，访问当前节点，然后进入右孩子。

## 后序非递归

# 全排列

 全排列就是所有可能出现在第一个位置的记录与剩下的元素的全排列 

```java
swap(arr, i, start);
fullSort(arr, start + 1, end);
swap(arr, i, start);
```

## 随机数

洗牌算法 1 / n！

```java
Random rd = new Random();
rd.nextInt(set.size());
```



# 位运算

## 加减法

```java
//加
public int getSum(int a, int b) {
    while(b!=0)
    {
        int res = (a&b)<<1;
        a = a^b;
        b = res;              
    }
    return a;
}
//减
public int minus(int a, int b) {
    b = getSum(~b, 1);
    return getSum(a, b);
}

```

## n = n & n - 1

将最右边的1置0

用处 : 判断一个数是否是2的幂

## 负数的位运算

右移左侧补1

左移右侧补0

# 排序

## 逆序对

求右边比自己小的数的个数，或者左边比自己大的数的个数

归并排序

leetcode315

```java
    private int[] temp;
    private int[] cnt;
    private int[] index;
    public List<Integer> countSmaller(int[] nums) {
        List<Integer> list = new ArrayList<Integer>();
        temp = new int[nums.length];
        cnt = new int[nums.length];
        index = new int[nums.length];

        for(int i = 0 ; i < nums.length; i++) {
        	index[i] = i;
        }
        merge(nums, 0, nums.length - 1);
        for(int i = 0; i < nums.length; i++) {
        	list.add(cnt[i]);
        }
        return list;
    }
    public void merge(int[] nums, int l, int r) {
    	if(l == r || l >= r) {
    		return;
    	}
    	int mid = (l + r) >>> 1;
    	if(l < mid) {
    		merge(nums, l, mid);
    	}
    	if(r > mid) {
    		merge(nums, mid + 1, r);
    	}
    	int i = l;
    	int j = mid + 1;
    	int ti = l;
    	while(i <= mid && j <= r) {
    		if(nums[index[i]] <= nums[index[j]]) {
    			temp[ti ++] = index[j];
    			j++;
    		}else {
				temp[ti++] = index[i];
				cnt[index[i]] += r - j + 1;
				i++;
			}
    	}
    	while(i <= mid) {
    		temp[ti++] = index[i++];
    	}
    	while (j <= r) {
    		temp[ti++] = index[j++];
		}
    	for(int k = l; k <= r; k++) {
    		index[k] = temp[k];
    	}
    }
```

## 链表归并

两个要点 1 快慢指针找中点

​				2 new一个head做头结点，返回head.next

## 手写堆排序

```java
public void heapify(int[] tree) {
    	int n = tree.length;
    	int i = (n >>> 1) - 1;
    	for(; i >= 0; i--) {
    		siftdown(tree, i, tree.length);
    	}
    }
    private void siftdown(int[] tree, int k, int length) {
		int temp = tree[k];
		int half = length >> 1;		
		while(k < half) {
			int child = (k << 1) + 1;
			if(child + 1 < length && tree[child] > tree[child + 1]) {
				child++;
			}
			if(temp <= tree[child]) { //temp不要写成tree[k]，此处多次写错，牢记
				break;
			}
			tree[k] = tree[child];
			k = child;
		}
		tree[k] = temp;
	}
	public void siftup(int[] tree, int k, int length) {
    	int temp = tree[k];
    	while (k >= 1) {
    		int father = (k - 1)>> 1;
			if(tree[father] <= temp) { //temp不要写成tree[k]，此处多次写错，牢记
				break;
			}
			tree[father] = tree[k];
			k = father;
		}
    	tree[k] = temp;
    }
```

java中用priorityqueue实现堆

queuemax = new PriorityQueue<Integer>((Integer A , Integer B) ->{return B - A;});
queuemin = new PriorityQueue<Integer>();

# 栈和队列

## 用队列表示栈

```java
public void push(int x) {
    int size = queue.size();
    queue.offer(x);
    while(size != 0) {
        queue.offer(queue.poll());
        size--;
    }
}
```

## 单调递增栈

简单来说，单调递增栈就是一个保持栈内元素为单调递增的栈。
 单调递增栈的典型范式为

```cpp
for(int i = 0; i < A.size(); i++){
  while(! in_stk.empty() && in_stk.top() > A[i]){
      in_stk.pop();
  }
  in_stk.push(A[i]);
}
```

**以O(n)的计算复杂度找到栈中每一个元素的前下界和后下届**

一个元素的**前下界**是指对这个元素“左边”的值，从这些值中找到一个greatest lower bound(也就是infimum)。

- 比如对于[ 3,7,8,4]这样一个数组来说。7的前下界是3；8的前下界是7；4的前下界是3；而3则没有前下界。

### 接雨水

leetcode42

找出最高点，然后左右两边分别维护单调递增栈计算

### 柱状图中最大矩形

leetcode84

维护一个单调递增栈，当加入新元素时，如果a[i-1] > a[i]]，就将大于a[i]的元素a[j]弹出，计算以a[j]为高的矩形面积。

leetcode 85

## 括号与栈

leetocde394 维护一个数字栈，一个字符串栈

# 排列组合

## 公式一：卡塔兰数

 **h(n)= h(0)\*h(n-1)+h(1)\*h(n-2) + ... + h(n-1)h(0) (n>=2)。** 

 **h(n)=C(2n,n)/(n+1)** 

## 公式二：

h(n) = 0 * h(0) + 1 * h(1) +...+n*h(n);

h(n) = n * 2 ^ (n - 1)

# 滑动窗口

如果是字符串 两个int[26]（有大写就int[128])，一个记录需求，一个记录当前。一个total记录需要的总字符数

通用：两个hashmap，一个做clear

leetcode 3,76,438

# 正则表达式

leetcode10

```java
else if(p.charAt(j - 1) == '*'){
    if(p.charAt(j - 2) == '.' || p.charAt(j - 2) == s.charAt(i - 1)) { //至少匹配一个
        if(dp[i - 1][j] == 1 || dp[i][j - 2] == 1) { //dp[i - 1][j] 匹配一个 dp[i][j - 2] 匹配0个
            dp[i][j] = 1;
        }
    }else { //匹配0个
        dp[i][j] = dp[i][j - 2]; //匹配0个
    }
}else{
    if((dp[i - 1][j - 1] == 1) && s.charAt(i - 1) == p.charAt(j - 1)){
        dp[i][j] = 1;
    }
}
```

# DP

## 编辑距离 leetcode 72

dp[i] [j] 考虑增删改三种操作

## 最长上升子序列

leetcode 300 334

o(n ^ 2)dp[i]表示以i结尾的子序列，每个dp[i]遍历0 到i-1求最大值

o(nlogn)tail[i]记录一个增序序列，每个nums[i]用二分查找法替换tail中第一个大于等于他的数。若nums[i]大于tail[len - 1]，则len++；填入新坑。返回len；

## 最长公共子序列

解决回文数时,将其reverse做

# DFS/BFS

## 单词接龙 leetcode126 127

关键点 

1对每一个单词每一个字母替换为其他25个字母，判断是否在给定set中

2可优化为双向bfs+DFS

## 最长增序路径

leetcode329

记忆化: 对于大量重复调用的问题，缓存其结果。
动态规划要求按照拓扑顺序解决子问题。对于很多问题，拓扑顺序与自然秩序一致。而对于那些并非如此的问题，需要首先执行拓扑排序。因此,对于复杂拓扑问题（如本题），使用记忆化搜索通常是更容易更好的选择。

思考：为什么本题不用visited

```java
//暴力
	private int[][] step = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};	
    public int longestIncreasingPath1(int[][] matrix) {
        int res = 0;
        for(int i = 0 ; i < matrix.length; i++) {
        	for(int j = 0; j < matrix[0].length; j++) {
        		res = Math.max(res, DFS1(matrix, i, j));
        	}
        }
        return res;
    }
	private int DFS1(int[][] matrix, int i, int j) {
		int length = 1;
		for(int k = 0; k < step.length; k++) {
			int x = i + step[k][0];
			int y = j + step[k][1];
			if(x >= 0 && x < matrix.length && y >= 0 && y < matrix[0].length && matrix[i][j] < matrix[x][y]) {
				length = Math.max(length, DFS1(matrix, x, y) + 1);
			}
		}
		return length;
	}
```

```java
	//记忆化搜索
	private int[][] step = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};	
	private int[][] cache;
	public int longestIncreasingPath(int[][] matrix) {
		if(matrix.length == 0) {
			return 0;
		}
		int res = 0;
		cache = new int[matrix.length][matrix[0].length];
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[0].length; j++) {
				res = Math.max(res, DFS(matrix, i, j));
			}
		}
		return res;
	}
	private int DFS(int[][] matrix, int i, int j) {
		if(cache[i][j] != 0) {
			return cache[i][j];
		}
		int length = 1;
		for(int k = 0; k < step.length; k++) {
			int x = i + step[k][0];
			int y = j + step[k][1];
			if(x >= 0 && x < matrix.length && y >= 0 && y < matrix[0].length && matrix[i][j] < matrix[x][y]) {
				length = Math.max(length, DFS(matrix, x, y) + 1);
			}			
		}
		cache[i][j] = length;
		return length;
	}
```

## 有向图判断是否有环路

1 BFS + 拓扑排序 入度

## 最短路径变种

![1585924451467](C:\Users\HASEE\AppData\Roaming\Typora\typora-user-images\1585924451467.png)

PDD2018机试第四题  

想法：用一个visited【x】【y】【1024】数组表示访问

bitmap位图表示10种钥匙是否出现

```java
public class s4 {
	public static class Node{
		int x;
		int y;
		int k;
		public Node(int x, int y, int k) {
			this.x = x;
			this.y = y;
			this.k = k;
 		}
	}
	static int[][][] visited;
	static int[][] step = new int[][]{{1, 0}, {-1, 0}, {0, -1}, {0, 1}};
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int x = scanner.nextInt();
		int y = scanner.nextInt();
		scanner.nextLine();
		char[][] graph = new char[x][y];
		int a = 0;
		int b = 0;
		for(int i = 0; i < x; i++) {
			graph[i] = scanner.nextLine().toCharArray();
		}
		for(int i = 0; i < x; i++) {
			for(int j = 0; j < y; j++) {
				// 0-墙，1-路，2-探险家的起始位置，3-迷宫的出口，大写字母-门，小写字母-对应大写字母所代表的门的钥匙
				if(graph[i][j] == '2') {
					a = i;
					b = j;
					break;
				}
			}
		}
		visited = new int[x][y][1024];
		visited[a][b][0] = 0;
		System.out.println(length(graph, a, b));
	}
	public static int length(char[][] graph, int i, int j) {
		Queue<Node> queue = new LinkedList<>();
		queue.offer(new Node(i, j, 0));
		int length = 0;
		while(!queue.isEmpty()) {
			int size = queue.size();
			length++;
			while(size-- > 0) {
				Node t = queue.poll();
				for(int k = 0; k < step.length; k++) {
					int newx = t.x + step[k][0];
					int newy = t.y + step[k][1];
					if(newx < 0 || newx >= graph.length || newy < 0 || newy >= graph[0].length) {
						continue;
					}					
					if((graph[newx][newy] == '1' || graph[newx][newy] == '2') && visited[newx][newy][t.k] == 0) {
						queue.offer(new Node(newx, newy, t.k));
						visited[newx][newy][t.k] = 1;
					}
					else if(graph[newx][newy] == '3') {
						return length;
					}else if(graph[newx][newy] >= 'a' && graph[newx][newy] <= 'z') {
						int newk = t.k | (1 << (graph[newx][newy] - 'a'));
						if(visited[newx][newy][newk] == 0) {
							queue.offer(new Node(newx, newy, newk));
							visited[newx][newy][newk] = 1;
						}
					}else if(graph[newx][newy] >= 'A' && graph[newx][newy] <= 'Z') {
						if(visited[newx][newy][t.k] == 0 && (t.k & (1 << (graph[newx][newy] - 'a'))) != 0) {
							queue.offer(new Node(newx, newy, t.k));
							visited[newx][newy][t.k] = 1;
						}
					}
				}
			}
		}
		return -1;
	}
}
```



 

# 重复数字

1 异或

2 循环链表 快慢指正

3 数学方法 1 - n 求和减当前和

4 普通的交换法

# 二分法

```java
//右边界可以收缩，那么取右中位数
int mid = (left + right + 1) >>> 1;
if(排除中位数逻辑){
    right = mid - 1;
}else{
    left = mid;
}

//左边界可以收缩，那么取左中位数
int mid = (left + right ) >>> 1
if(排除中位数逻辑){
    left = mid + 1;
}else{
    right = mid;
}
```

69 744 540 278 153 34

![image.png](https://pic.leetcode-cn.com/4f735d3ea9826097b96e53f4c6cc63f5d4e6341d3fd85ef6aa161a72971ee0bf-image.png)

# 扫描线法

leetcode218

# 贪心

重叠区间问题 435 452 

# 最长连续子序列

先全部添加到set，然后遍历set，看cnt+1在不在set里。

关键代码：

if(set.contains(t - 1)){

​        continue;

​      }