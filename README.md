# HttpClient
兼容HTTPClient  访问HTTPS协议，支持SNI

#探究 https 自签名的双向校验，
主要实现：
1、okhttp3 
2、HttpURLConnection
3、HTTPClient
 代码相应的文家中能找到，主要头疼的是HTTPClient 的实现，毕竟废弃了这么长时间，
