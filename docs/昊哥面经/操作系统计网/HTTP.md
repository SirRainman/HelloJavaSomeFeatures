# HTTP

# 一 、基础概念

http协议是应用层协议（服务器先初始化一个socket,与端口绑定,对端口进行监听,调用阻塞,等待客户端的连接
初始化客户端的socket,与服务器的socket连接）。

HTTP协议使用TCP协议进行传输，在应用层协议发起交互之前，首先是TCP的三次握手。完成了TCP三次握手后，客户端会向服务器发出一个请求报文。

## URI

URI 包含 URL 和 URN。

![image-20200109205842828](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200109205842828.png)

## 请求和响应报文

http的报文分为请求报文和响应报文。

### 1. 请求报文

请求报文包含四部分：请求行，请求头，请求体

![img](https://segmentfault.com/img/remote/1460000013229039?w=735&h=272)

​	请求行

​		请求方法+请求URL+HTTP版本

​		GET [www.myweb.com/home/img/](http://www.myweb.com/home/img/) HTTP 1.1	

​		请求方法包括：GET，POST，PUT，HEAD，DELETE等

​		url用于标识请求资源的位置

​		HTTP版本基本都为1.1 部分服务器使用1.0的版本

​	请求首部字段  

​        Accept：可处理的媒体类型

​		Host：请求资源所在服务器

​		Range：实体的字节请求范围

​	通用首部字段

​		Cache-Control：控制缓存的行为

​		Connection：逐跳首部、连接的管理

​		Via：代理服务器的相关信息

​		Warning：错误通知

​	实体首部字段

​		Allow：资源可支持的HTTP方法

​		Content-Encoding：实体主体适用的编码方式

​		Content-Length：实体主体的长度

​	其他

### 2. 响应报文

![image-20200109205917455](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200109205917455.png)

响应报文包含四部分：

![img](https://segmentfault.com/img/remote/1460000013229040?w=724&h=260)

- a、状态行：包含HTTP版本、状态码、状态码的原因短语

- b、响应头：首部字段

- c、响应体

- d、空行 

  Response

  ​	状态行

  ​		HTTP版本+状态码+状态码简单描述

  ​		HTTP 1.1 200 OK

  ​	响应首部字段

  ​		Accept-Ranges：是否接受字节范围请求（如果不接受，返回完整的请求对象）

  ​		Location：重定向至指定URL

  ​		Server：HTTP服务器的安装信息

  ​	通用首部字段

  ​		和Request一致，因此称为通用

  ​	实体首部字段

  ​		和Request一致，和实体内容相关

# 二、HTTP 方法

客户端发送的 **请求报文** 第一行为请求行，包含了方法字段。

## GET

> 获取资源

当前网络请求中，绝大部分使用的是 GET 方法。

## HEAD

> 获取报文首部

和 GET 方法类似，但是不返回报文实体主体部分。

主要用于确认 URL 的有效性以及资源更新的日期时间等。

## POST

> 传输实体主体

POST 主要用来传输数据，而 GET 主要用来获取资源。

## PUT

> 上传文件

由于自身不带验证机制，任何人都可以上传文件，因此存在安全性问题，一般不使用该方法。

```
PUT /new.html HTTP/1.1
Host: example.com
Content-type: text/html
Content-length: 16

<p>New File</p>
```

**PATCH**

> 对资源进行部分修改

PUT 也可以用于修改资源，但是只能完全替代原始资源，PATCH 允许部分修改。

```
PATCH /file.txt HTTP/1.1
Host: www.example.com
Content-Type: application/example
If-Match: "e0023aa4e"
Content-Length: 100

[description of changes]
```

**DELETE**

> 删除文件

与 PUT 功能相反，并且同样不带验证机制。

```
DELETE /file.html HTTP/1.1
```

**OPTIONS**

> 查询支持的方法

查询指定的 URL 能够支持的方法。

会返回 `Allow: GET, POST, HEAD, OPTIONS` 这样的内容。

**CONNECT**

> 要求在与代理服务器通信时建立隧道

使用 SSL（Secure Sockets Layer，安全套接层）和 TLS（Transport Layer Security，传输层安全）协议把通信内容加密后经网络隧道传输。

```
CONNECT www.example.com:443 HTTP/1.1
```

[![img](https://camo.githubusercontent.com/1fdcf70fd0e44a9b77beecc8983b4e2e8afde712/68747470733a2f2f63732d6e6f7465732d313235363130393739362e636f732e61702d6775616e677a686f752e6d7971636c6f75642e636f6d2f64633030663730652d633563382d346432302d626166312d3264373030313461393765332e6a7067)](https://camo.githubusercontent.com/1fdcf70fd0e44a9b77beecc8983b4e2e8afde712/68747470733a2f2f63732d6e6f7465732d313235363130393739362e636f732e61702d6775616e677a686f752e6d7971636c6f75642e636f6d2f64633030663730652d633563382d346432302d626166312d3264373030313461393765332e6a7067)



**TRACE**

> 追踪路径

服务器会将通信路径返回给客户端。

发送请求时，在 Max-Forwards 首部字段中填入数值，每经过一个服务器就会减 1，当数值为 0 时就停止传输。

通常不会使用 TRACE，并且它容易受到 XST 攻击（Cross-Site Tracing，跨站追踪）。

## GET和POST比较

### 参数

GET 和 POST 的请求都能使用额外的参数，但是 GET **的参数是以查询字符串出现在 URL 中，而 POST 的参数存储在RequsetBody中**。不能因为 POST 参数存储在实体主体中就认为它的安全性更高，因为照样可以通过一些抓包工具（Fiddler）查看。

因为 URL 只支持 ASCII 码，因此 GET 的参数中如果存在中文等字符就需要先进行编码。例如 `中文` 会转换为 `%E4%B8%AD%E6%96%87`，而空格会转换为 `%20`。POST 参数支持标准字符集。

```
GET /test/demo_form.asp?name1=value1&name2=value2 HTTP/1.1
POST /test/demo_form.asp HTTP/1.1
Host: w3schools.com
name1=value1&name2=value2
```

### 安全，幂等性

**安全指的不是参数安全，而是不会改变服务器状态，也就是说它只是可读的。**

GET 方法是安全的，而 POST 却不是，因为 POST 的目的是传送实体主体内容，这个内容可能是用户上传的表单数据，上传成功之后，服务器可能把这个数据存储到数据库中，因此状态也就发生了改变。

安全的方法除了 GET 之外还有：HEAD、OPTIONS。

不安全的方法除了 POST 之外还有 PUT、DELETE。

**幂等的 HTTP 方法，同样的请求被执行一次与连续执行多次的效果是一样的，服务器的状态也是一样的**。换句话说就是，幂等方法不应该具有副作用（统计用途除外）。

所有的安全方法也都是幂等的（delete不安全也是幂等）。

在正确实现的条件下，GET，HEAD，PUT 和 DELETE 等方法都是幂等的，而 POST 方法不是。

GET /pageX HTTP/1.1 是幂等的，连续调用多次，客户端接收到的结果都是一样的：

```
GET /pageX HTTP/1.1
GET /pageX HTTP/1.1
GET /pageX HTTP/1.1
GET /pageX HTTP/1.1
```

POST /add_row HTTP/1.1 不是幂等的，如果调用多次，就会增加多行记录：

```
POST /add_row HTTP/1.1   -> Adds a 1nd row
POST /add_row HTTP/1.1   -> Adds a 2nd row
POST /add_row HTTP/1.1   -> Adds a 3rd row
```

DELETE /idX/delete HTTP/1.1 是幂等的，即使不同的请求接收到的状态码不一样：

```
DELETE /idX/delete HTTP/1.1   -> Returns 200 if idX exists
DELETE /idX/delete HTTP/1.1   -> Returns 404 as it just got deleted
DELETE /idX/delete HTTP/1.1   -> Returns 404
```

### 可缓存

如果要对响应进行缓存，需要满足以下条件：

- 请求报文的 HTTP 方法本身是可缓存的，包括 GET 和 HEAD，但是 PUT 和 DELETE 不可缓存，POST 在多数情况下不可缓存的。
- 响应报文的状态码是可缓存的，包括：200, 203, 204, 206, 300, 301, 404, 405, 410, 414, and 501。
- 响应报文的 Cache-Control 首部字段没有指定不进行缓存。

### XMLHttpRequest

为了阐述 POST 和 GET 的另一个区别，需要先了解 XMLHttpRequest：

> XMLHttpRequest 是一个 API，它为客户端提供了在客户端和服务器之间传输数据的功能。它提供了一个通过 URL 来获取数据的简单方式，并且不会使整个页面刷新。这使得网页只更新一部分页面而不会打扰到用户。XMLHttpRequest 在 AJAX 中被大量使用。

- 在使用 XMLHttpRequest 的 POST 方法时，浏览器会先发送 Header 再发送 Data。但并不是所有浏览器会这么做，例如火狐就不会。
- 而 GET 方法 Header 和 Data 会一起发送。

# 三、HTTP 状态码

服务器返回的 **响应报文** 中第一行为状态行，包含了状态码以及原因短语，用来告知客户端请求的结果。

| 状态码 | 类别                             | 含义                       |
| ------ | -------------------------------- | -------------------------- |
| 1XX    | Informational（信息性状态码）    | 接收的请求正在处理         |
| 2XX    | Success（成功状态码）            | 请求正常处理完毕           |
| 3XX    | Redirection（重定向状态码）      | 需要进行附加操作以完成请求 |
| 4XX    | Client Error（客户端错误状态码） | 服务器无法处理请求         |
| 5XX    | Server Error（服务器错误状态码） | 服务器处理请求出错         |

**1XX 信息**

- **100 Continue** ：表明到目前为止都很正常，客户端可以继续发送请求或者忽略这个响应699+

**2XX 成功**

- **200 OK**
- **204 No Content** ：请求已经成功处理，但是返回的响应报文不包含实体的主体部分。一般在只需要从客户端往服务器发送信息，而不需要返回数据时使用。
- **206 Partial Content** ：表示客户端进行了范围请求，响应报文包含由 Content-Range 指定范围的实体内容。

**3XX 重定向**

- **301 Moved Permanently** ：永久性重定向，
- **302 Found** ：临时性重定向，（发生两次请求）
- **303 See Other** ：和 302 有着相同的功能，但是 303 明确要求客户端应该采用 GET 方法获取资源。
- 注：虽然 HTTP 协议规定 301、302 状态下重定向时不允许把 POST 方法改成 GET 方法，但是大多数浏览器都会在 301、302 和 303 状态下的重定向把 POST 方法改成 GET 方法。
- **304 Not Modified** ：如果请求报文首部包含一些条件，例如：If-Match，If-Modified-Since，If-None-Match，If-Range，If-Unmodified-Since，如果不满足条件，则服务器会返回 304 状态码。
- **307 Temporary Redirect** ：临时重定向，与 302 的含义类似，但是 307 要求浏览器不会把重定向请求的 POST 方法改成 GET 方法。

**4XX 客户端错误**

- **400 Bad Request** ：请求报文中存在语法错误。
- **401 Unauthorized** ：该状态码表示发送的请求需要有认证信息（BASIC 认证、DIGEST 认证）。如果之前已进行过一次请求，则表示用户认证失败。
- **403 Forbidden** ：请求被拒绝。
- **404 Not Found**

**5XX 服务器错误**

- **500 Internal Server Error** ：服务器正在执行请求时发生错误。
- 502 bad gateway 顾名思义 网关错误 后端服务器tomcat没有起来，集群压力大
- **503 Service Unavailable** ：服务器暂时处于超负载或正在进行停机维护，现在无法处理请求。
-  **504 Gateway Timeout** 由作为代理或网关的服务器使用，表示不能及时地从远程服务器获得应答。（HTTP 1.1新） 

# 四、HTTP 首部

有 4 种类型的首部字段：通用首部字段、请求首部字段、响应首部字段和实体首部字段。

各种首部字段及其含义如下（不需要全记，仅供查阅）：

**通用首部字段**

| Cache-Control     | 控制缓存的行为                             |
| ----------------- | ------------------------------------------ |
| 首部字段名        | 说明                                       |
| Connection        | 控制不再转发给代理的首部字段、管理持久连接 |
| Date              | 创建报文的日期时间                         |
| Pragma            | 报文指令                                   |
| Trailer           | 报文末端的首部一览                         |
| Transfer-Encoding | 指定报文主体的传输编码方式                 |
| Upgrade           | 升级为其他协议                             |
| Via               | 代理服务器的相关信息                       |
| Warning           | 错误通知                                   |

**请求首部字段**

| 首部字段名          | 说明                                            |
| ------------------- | ----------------------------------------------- |
| Accept              | 用户代理可处理的媒体类型                        |
| Accept-Charset      | 优先的字符集                                    |
| Accept-Encoding     | 优先的内容编码                                  |
| Accept-Language     | 优先的语言（自然语言）                          |
| Authorization       | Web 认证信息                                    |
| Expect              | 期待服务器的特定行为                            |
| From                | 用户的电子邮箱地址                              |
| Host                | 请求资源所在服务器                              |
| If-Match            | 比较实体标记（ETag）                            |
| If-Modified-Since   | 比较资源的更新时间                              |
| If-None-Match       | 比较实体标记（与 If-Match 相反）                |
| If-Range            | 资源未更新时发送实体 Byte 的范围请求            |
| If-Unmodified-Since | 比较资源的更新时间（与 If-Modified-Since 相反） |
| Max-Forwards        | 最大传输逐跳数                                  |
| Proxy-Authorization | 代理服务器要求客户端的认证信息                  |
| Range               | 实体的字节范围请求                              |
| Referer             | 对请求中 URI 的原始获取方                       |
| TE                  | 传输编码的优先级                                |
| User-Agent          | HTTP 客户端程序的信息                           |

**响应首部字段**

| 首部字段名         | 说明                         |
| ------------------ | ---------------------------- |
| Accept-Ranges      | 是否接受字节范围请求         |
| Age                | 推算资源创建经过时间         |
| ETag               | 资源的匹配信息               |
| Location           | 令客户端重定向至指定 URI     |
| Proxy-Authenticate | 代理服务器对客户端的认证信息 |
| Retry-After        | 对再次发起请求的时机要求     |
| Server             | HTTP 服务器的安装信息        |
| Vary               | 代理服务器缓存的管理信息     |
| WWW-Authenticate   | 服务器对客户端的认证信息     |

**实体首部字段**

| 首部字段名       | 说明                   |
| ---------------- | ---------------------- |
| Allow            | 资源可支持的 HTTP 方法 |
| Content-Encoding | 实体主体适用的编码方式 |
| Content-Language | 实体主体的自然语言     |
| Content-Length   | 实体主体的大小         |
| Content-Location | 替代对应资源的 URI     |
| Content-MD5      | 实体主体的报文摘要     |
| Content-Range    | 实体主体的位置范围     |
| Content-Type     | 实体主体的媒体类型     |
| Expires          | 实体主体过期的日期时间 |
| Last-Modified    | 资源的最后修改日期时间 |

# 五、具体应用

## 连接管理

![image-20200109210422225](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200109210422225.png)

### 1. 短连接与长连接

当浏览器访问一个包含多张图片的 HTML 页面时，除了请求访问的 HTML 页面资源，还会请求图片资源。如果每进行一次 HTTP 通信就要新建一个 TCP 连接，那么开销会很大。

**长连接只需要建立一次 TCP 连接就能进行多次 HTTP 通信**。

- 从 HTTP/1.1 开始默认是长连接的，如果要断开连接，需要由客户端或者服务器端提出断开，使用 `Connection : close`；
- 在 HTTP/1.1 之前默认是短连接的，如果需要使用长连接，则使用 `Connection : Keep-Alive`。

### 2. 流水线

流水线是在同一条长连接上**连续发出请求，而不用等待响应返回**，这样可以减少延迟。服务器端必须按照接收到客户端请求的**先后顺序**依次回送响应结果。

会造成**队头阻塞 （Head-of-line blocking）**的问题，即靠前的响应一旦阻塞，会耽误后续的响应发送。

## Cookie和Session

HTTP 协议对于交互性场景没有记忆能力。因此 HTTP/1.1 引入 Cookie 来保存状态信息。

### cookie

Cookie 是服务器发给浏览器并保存在本地的数据，浏览器再次访问服务器会携带Cookie。由于之后每次请求都会需要携带 Cookie 数据，因此会带来额外的性能开销（尤其是在移动环境下）。

1. **用途**

- 会话状态管理（如用户登录状态、购物车、游戏分数或其它需要记录的信息）
- 个性化设置（如用户自定义设置、主题等）
- 浏览器行为跟踪（如跟踪分析用户行为等）

2. **创建过程**

服务器发送的响应报文包含 Set-Cookie 首部字段，客户端得到响应报文后把 Cookie 内容保存到浏览器中。

```
HTTP/1.0 200 OK
Content-type: text/html
Set-Cookie: yummy_cookie=choco
Set-Cookie: tasty_cookie=strawberry

[page content]
```

客户端之后对同一个服务器发送请求时，会从浏览器中取出 Cookie 信息并通过 Cookie 请求首部字段发送给服务器。

```
GET /sample_page.html HTTP/1.1
Host: www.example.org
Cookie: yummy_cookie=choco; tasty_cookie=strawberry
```

3. **分类**

- 会话期 Cookie：浏览器关闭之后它会被自动删除，也就是说它仅在会话期内有效。
- 持久性 Cookie：指定过期时间（Expires）或有效期（max-age）之后就成为了持久性的 Cookie。

```
Set-Cookie: id=a3fWa; Expires=Wed, 21 Oct 2015 07:28:00 GMT;
```

4. **作用域**

Domain 标识指定了哪些主机可以接受 Cookie。如果不指定，默认为当前文档的主机（不包含子域名）。如果指定了 Domain，则一般包含子域名。例如，如果设置 Domain=mozilla.org，则 Cookie 也包含在子域名中（如 developer.mozilla.org）。

Path 标识指定了主机下的哪些路径可以接受 Cookie（该 URL 路径必须存在于请求 URL 中）。以字符 %x2F ("/") 作为路径分隔符，子路径也会被匹配。例如，设置 Path=/docs，则以下地址都会匹配：

- /docs
- /docs/Web/
- /docs/Web/HTTP

### Session

除了可以将用户信息通过 Cookie 存储在用户浏览器中，也可以利用 Session 存储在服务器端，存储在服务器端的信息更加安全。

Session 可以存储在服务器上的文件、数据库或者内存中。

四种方法实现分布式session：复制，hash（粘性），redis，mysql

以秒杀系统为例，使用 Session 维护用户登录状态的过程如下：

- 用户进行登录时，用户提交包含用户名和密码的表单，放入 HTTP 请求报文中；
- 服务器验证该用户名和密码，如果正确则用UUID生成token（SessionId）来标识用户，以Sessionid为key，用户信息为value存储到 Redis 中；
- 服务器生成Cookie，将token写入Cookie。返回的响应报文的 Set-Cookie 首部字段包含了这个 Session ID，客户端收到响应报文之后将该 Cookie 值存入浏览器中；
- 客户端之后对同一个服务器进行请求时会包含该 Cookie 值，服务器收到之后提取出 Session ID，从 Redis 中取出用户信息，继续之前的业务操作。

应该注意 Session ID 的安全性问题，不能让它被恶意攻击者轻易获取，那么就不能产生一个容易被猜到的 Session ID 值。此外，还需要经常重新生成 Session ID。在对安全性要求极高的场景下，例如转账等操作，除了使用 Session 管理用户状态之外，还需要对用户进行重新验证，比如重新输入密码，或者使用短信验证码等方式。

### 浏览器禁用 Cookie

此时无法使用 Cookie 来保存用户信息，也不能再将 Session ID 存放到 Cookie 中，而是使用 URL 重写技术，将 Session ID 作为 URL 的参数进行传递。

### Cookie 与 Session 选择

- Cookie 只能存储 ASCII 码字符串，而 Session 则可以存储任何类型的数据，因此在考虑数据复杂性时首选 Session；
- **Cookie 存储在浏览器中，安全性低**，容易被恶意查看。如果非要将一些隐私数据存在 Cookie 中，可以将 Cookie 值进行加密，然后在服务器进行解密；
- 对于大型网站，如果用户所有的信息都存储在 Session 中，那么开销是非常大的，因此不建议将所有的用户信息都存储到 Session 中。

## 其他特点

**分块传输编码**

Chunked Transfer Encoding，可以把数据分割成多块，让浏览器逐步显示页面。

**多部分对象集合**

一份报文主体内可含有多种类型的实体同时发送，每个部分之间用 boundary 字段定义的分隔符进行分隔，每个部分都可以有首部字段。

**虚拟主机**

HTTP/1.1 使用虚拟主机技术，使得一台服务器拥有多个域名，并且在逻辑上可以看成多个服务器。

**通信数据转发**

1. 代理

代理服务器接受客户端的请求，并且转发给其它服务器。

使用代理的主要目的是：

- 缓存
- 负载均衡
- 网络访问控制
- 访问日志记录

代理服务器分为正向代理和反向代理两种：

- 用户察觉得到正向代理的存在。

- 而反向代理一般位于内部网络中，用户察觉不到。

2. 网关

与代理服务器不同的是，网关服务器会将 HTTP 转化为其它协议进行通信，从而请求其它非 HTTP 服务器的服务。

3. 隧道

使用 SSL 等加密手段，在客户端和服务器之间建立一条安全的通信线路。

# 六、HTTPS（重点）

HTTP 有以下安全性问题：

- 使用明文进行通信，内容可能会被**窃听**；
- 不验证通信方的身份，通信方的身份有可能遭遇**伪装**；
- 无法证明报文的完整性，报文有可能遭**篡改**。

HTTPS 让 HTTP 先和 **SSL**（Secure Sockets Layer）通信，再由 SSL 和 TCP 通信，也就是说 HTTPS 使用了隧道进行通信。

## 加密

### 1. 对称密钥加密

对称密钥加密（Symmetric-Key Encryption），加密和解密使用**同一密钥**。

- 优点：运算速度**快**；
- 缺点：无法安全地将密钥传输给通信方。

![img](https://images0.cnblogs.com/blog/603146/201402/111538337847046.png)



### 2.非对称密钥加密

非对称密钥加密，又称公开密钥加密（Public-Key Encryption），加密和解密使用不同的密钥。

公开密钥所有人都可以获得，通信**发送方获得接收方的公开密钥之后，就可以使用公开密钥进行加密，接收方收到通信内容后使用私有密钥解密。**

非对称密钥除了用来加密，还可以用来进行签名。因为私有密钥无法被其他人获取，因此通信发送方使用其私有密钥进行签名，通信接收方使用发送方的公开密钥对签名进行解密，就能判断这个签名是否正确。

- 优点：可以更安全地将公开密钥传输给通信发送方；
- 缺点：运算速度**慢**。

![image-20200109211132953](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200109211132953.png)



## HTTPS 采用的加密方式

- 所有信息都是加密传播，第三方无法窃听。
- 具有校验机制，一旦被篡改，通信双方会立刻发现。
- 配备身份证书，防止身份被冒充。

SSL/TLS协议的基本思路是采用非对称加密法，也就是说，客户端先向服务器端索要公钥，然后用公钥加密信息，服务器收到密文后，用自己的私钥解密。
 但是，这里有两个问题：

1. 如何保证公钥不被篡改？
    解决方法：将公钥放在数字证书中。只要证书是可信的，公钥就是可信的。
2. 公钥加密计算量太大，如何减少耗用的时间？
    解决方法：每一次对话（session），客户端和服务器端都生成一个"对话密钥"（session key），用它来加密信息。由于"对话密钥"是对称加密，所以运算速度非常快，而服务器公钥只用于加密"对话密钥"本身，这样就减少了加密运算的消耗时间。
3. ![img](https://img-blog.csdn.net/20180609180728137?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2E0MDc0Nzk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

1）客户端发起一个https请求，连接到服务器的443端口。

2）服务端把自己的信息以数字证书的形式返回给客户端（证书内容有密钥公钥，网站地址，证书颁发机构，失效日期等）。证书中有一个公钥来加密信息，私钥由服务器持有。

3）验证证书的合法性

客户端收到证书后向数字认证机构验证证书的合法性。

4）

客户端生成 Pre-master sercret 对称密钥，用公钥加密后发送给服务端。服务端用私钥解密。之后双方用这个对称密钥加密解密。



### **https握手的过程**

准备工作：

​	服务端生成公钥，在CA（第三方数字证书认证机构）进行注册，机构会对申请的公钥生成数字签名并发布，用于防止服务端时伪造的。

​	多数浏览器开发商在发布版本时，会预置常用的认证机关的公开密钥。

 

**HTTPS通信步骤**

1. 客户端发起握手请求：ClientHello，同时发送自己的SSL版本，加密组件列表等

2. 服务端响应握手请求：ServerHello，包含了服务端筛选后可用的加密组件信息

3. 服务端发送Certificate报文，里面含有公钥和证书

4. 服务端发送：Server Hello Done报文，最初截断握手协商结束

 

5. 第一次握手后，客户端生成用于对称加密的密钥Pre-master secret，并使用服务端的公钥加密，通过Client Key Exchange报文发送给服务端

6. 接着客户端发送Change Cipher Spec报文，提示服务器之后的报文会采用Pre-master secret进行对称加密

7. 客户端发送Finished报文，其中包括至今全部报文的整体校验值（验证完整性，加密的工作已经在前面完成了，但是方式服务端没有收到或者收到的报文有损）。如果服务器可以正确解密此次报文，则认为此次握手协商成功。

8. 服务器同样发送 Change Cipher Spec报文

9. 服务器发送Finished报文

10. Finished报文交换完成后，SSL连接建立完成

11. 应用层协议通信，发送HTTP报文

12. 客户端断开连接

在上述流程中，应用层发送数据的时候会附加MAC（Message Authentication Code）的报文摘要，用于验证完整性。

### **https证书签发过程**

\1. 服务器把自身公钥登录至数字证书认证机构

\2. 数字证书认证机构用自己的私钥对服务器的公钥生成数字签名并颁发公钥证书

\3. 客户端从服务端获取公钥证书，向数字证书认证机构确认真实性

\4. 公钥确认无误后用公钥加密后续的对称密钥

\5. 服务器用私钥进行解密

## 认证

通过使用 **证书** 来对通信方进行认证。

数字证书认证机构（CA，Certificate Authority）是客户端与服务器双方都可信赖的第三方机构。

服务器的运营人员向 CA 提出公开密钥的申请，CA 在判明提出申请者的身份之后，会对已申请的公开密钥做数字签名，然后分配这个已签名的公开密钥，并将该公开密钥放入公开密钥证书后绑定在一起。

进行 HTTPS 通信时，服务器会把证书发送给客户端。客户端取得其中的公开密钥之后，先使用数字签名进行验证，如果验证通过，就可以开始通信了。

## 报文摘要完整性保护

SSL 提供**报文摘要功能**来进行完整性保护。

HTTP 也提供了 MD5 报文摘要功能，但不是安全的。例如报文内容被篡改之后，同时重新计算 MD5 的值，通信接收方是无法意识到发生了篡改。

HTTPS 的报文摘要功能之所以安全，是因为它结合了加密和认证这两个操作。试想一下，加密之后的报文，遭到篡改之后，也很难重新计算报文摘要，因为无法轻易获取明文。

**HTTPS 的缺点**

- 因为需要进行加密解密等过程，因此速度会更慢；
- 需要支付证书授权的高额费用。

# 七、HTTP/1.1 

- **默认是长连接，connection默认是keep-alive。长连接只需要建立一次 TCP 连接就能进行多次 HTTP 通信。**（ 一个包含有许多图像的网页文件的多个请求和应答可以在一个连接中传输，但每个单独的网页文件的请求和应答仍然需要使用各自的连接。）
- **支持流水线** ，客户端可以同时发出多个HTTP请求，而不用一个个等待响应  （缺点是队头阻塞问题， 服务器端必须按照接收到请求顺序依次回送响应结果，以保证客户端能够区分出每次请求的响应内容 ）
- 新增cookie，cache-control字段
- **断点续传**
  - 利用HTTP消息头使用分块传输编码， Content-Length消息头字段表示数据的长度。 
- 支持虚拟主机
- 新增状态码 100
- 新增缓存处理指令 max-age

# 八、HTTP/2.0

## HTTP/1.x 缺陷

HTTP/1.x 实现简单是以牺牲性能为代价的：

- 虽然它支持流水线，无需响应就可以发送下一个请求，但对多个请求还是会顺序处理，否则无法区分响应内容。**存在队头阻塞问题。**
- 虽然支持长连接，但是单独的网页文件请求和应答仍然需要使用各自的连接。
- 不会压缩请求和响应首部，从而导致不必要的网络流量；
- 不支持有效的资源优先级，致使底层 TCP 连接的利用率低下。

## 二进制分帧层

之前的HTTP的版本中，我们传输数据方式--**文本传输。**

HTTP/2在 应用层(HTTP/2)和传输层(TCP or UDP)之间增加一个二进制分帧层。 从而实现多路复用。

![img](https://www.mwcxs.top/static/upload/pics/2019/2/279YC4iuosAlZXshovHPYXWnX1.png)



**多路复用**

HTTP/2.0 在一个TCP连接中存在多条流。每条流由 HEADERS 帧和 DATA 帧组成，它们都是二进制格式的。

不同流的帧可以交错发送，然后根据帧头数据流标识符重新组装。发送多个请求，对端可以通过帧中的标识知道属于哪个请求。从而避免 HTTP 旧版本中的队头阻塞问题，极大的提高传输性能。

![img](https://www.mwcxs.top/static/upload/pics/2019/2/27eEKU9cpx8hlWvTRL-GjzVhIR.png)

在通信过程中，只会有一个 TCP 连接存在，它承载了任意数量的双向数据流（Stream）。

- 一个数据流（Stream）都有一个唯一标识符和可选的优先级信息，用于承载双向信息。
- 消息（Message）是与逻辑请求或响应对应的完整的一系列帧。
- 帧（Frame）是最小的通信单位，来自不同数据流的帧可以交错发送，然后再根据每个帧头的数据流标识符重新组装。

## 服务端推送

HTTP/2.0 在客户端请求一个资源时，会把相关的资源一起发送给客户端，客户端就不需要再次发起请求了。例如客户端请求 page.html 页面，服务端就把 script.js 和 style.css 等与之相关的资源一起发给客户端。



## 首部压缩

HTTP/1.1 的首部带有大量信息，而且每次都要重复发送。

HTTP/2.0 要求客户端和服务器同时维护和更新一个包含之前见过的首部字段表，从而避免了重复传输。

不仅如此，HTTP/2.0 也使用 Huffman 编码对首部字段进行压缩。

#  问题

**说一下你知道的Http的请求头和返回码**

**你知道 Http 状态码？302 是代表啥意思？502 是代表啥意思？**

200，302，401，403，404，502，503

**http协议解释一下，tcp/ip协议，三次握手（为什么是三次而不是四次），四次分手（每一步的作用，服务端什么时候close wait）**

**计算机网络：TCP三次握手、四次挥手以及为什么这么干、time wait的作用**

**HTTP，TCP三次握手**

http协议是应用层协议，使用TCP协议传输，TCP三次握手后，客户端会向服务器发出一个请求报文。

TCP/IP协议不仅仅指的是[TCP](https://baike.baidu.com/item/TCP/33012) 和[IP](https://baike.baidu.com/item/IP/224599)两个协议，而是指一个由[FTP](https://baike.baidu.com/item/FTP/13839)、[SMTP](https://baike.baidu.com/item/SMTP/175887)、TCP、[UDP](https://baike.baidu.com/item/UDP/571511)、IP等协议构成的协议簇，五层。

**HTTPS握手的详细过程、为什么要用非对称密钥。**

**http的post和get方法有什么区别？**

参数位置 ，安全，幂等性

http请求 header有哪些关键词

幂等的场景

**影响一个http服务器的最大http请求数有哪些因素** 

 HTTP通信依赖TCP连接，一个TCP连接就是一个套接字， 对linux系统来说就是一个文件，一个进程可以打开的文件数量是有限的。

time-wait的影响。 用Nginx通常都是让其工作在代理模式，使用随机端口来向后端发起请求，而系统可用随机端口范围是一定的。time-wait期间端口号不可用，可能会导致端口号耗尽。

# 参考资料

- 上野宣. 图解 HTTP[M]. 人民邮电出版社, 2014.
- [MDN : HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP)
- [HTTP/2 简介](https://developers.google.com/web/fundamentals/performance/http2/?hl=zh-cn)
- [htmlspecialchars](http://php.net/manual/zh/function.htmlspecialchars.php)
- [Difference between file URI and URL in java](http://java2db.com/java-io/how-to-get-and-the-difference-between-file-uri-and-url-in-java)
- [How to Fix SQL Injection Using Java PreparedStatement & CallableStatement](https://software-security.sans.org/developer-how-to/fix-sql-injection-in-java-using-prepared-callable-statement)
- [浅谈 HTTP 中 Get 与 Post 的区别](https://www.cnblogs.com/hyddd/archive/2009/03/31/1426026.html)
- [Are http:// and www really necessary?](https://www.webdancers.com/are-http-and-www-necesary/)
- [HTTP (HyperText Transfer Protocol)](https://www.ntu.edu.sg/home/ehchua/programming/webprogramming/HTTP_Basics.html)
- [Web-VPN: Secure Proxies with SPDY & Chrome](https://www.igvita.com/2011/12/01/web-vpn-secure-proxies-with-spdy-chrome/)
- [File:HTTP persistent connection.svg](http://en.wikipedia.org/wiki/File:HTTP_persistent_connection.svg)
- [Proxy server](https://en.wikipedia.org/wiki/Proxy_server)
- [What Is This HTTPS/SSL Thing And Why Should You Care?](https://www.x-cart.com/blog/what-is-https-and-ssl.html)
- [What is SSL Offloading?](https://securebox.comodo.com/ssl-sniffing/ssl-offloading/)
- [Sun Directory Server Enterprise Edition 7.0 Reference - Key Encryption](https://docs.oracle.com/cd/E19424-01/820-4811/6ng8i26bn/index.html)
- [An Introduction to Mutual SSL Authentication](https://www.codeproject.com/Articles/326574/An-Introduction-to-Mutual-SSL-Authentication)
- [The Difference Between URLs and URIs](https://danielmiessler.com/study/url-uri/)
- [Cookie 与 Session 的区别](https://juejin.im/entry/5766c29d6be3ff006a31b84e#comment)
- [COOKIE 和 SESSION 有什么区别](https://www.zhihu.com/question/19786827)
- [Cookie/Session 的机制与安全](https://harttle.land/2015/08/10/cookie-session.html)
- [HTTPS 证书原理](https://shijianan.com/2017/06/11/https/)
- [What is the difference between a URI, a URL and a URN?](https://stackoverflow.com/questions/176264/what-is-the-difference-between-a-uri-a-url-and-a-urn)
- [XMLHttpRequest](https://developer.mozilla.org/zh-CN/docs/Web/API/XMLHttpRequest)
- [XMLHttpRequest (XHR) Uses Multiple Packets for HTTP POST?](https://blog.josephscott.org/2009/08/27/xmlhttprequest-xhr-uses-multiple-packets-for-http-post/)
- [Symmetric vs. Asymmetric Encryption – What are differences?](https://www.ssl2buy.com/wiki/symmetric-vs-asymmetric-encryption-what-are-differences)
- [Web 性能优化与 HTTP/2](https://www.kancloud.cn/digest/web-performance-http2)
- [HTTP/2 简介](https://developers.google.com/web/fundamentals/performance/http2/?hl=zh-cn)