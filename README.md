# uid-generator-spring-boot-starter

> 百度唯一UID生成器Springboot自动化配置

默认情况下不需要配置任何参数，直接注入UidGenerator对象即可使用。

## 版本说明

该项目版本与[uid-generator](https://github.com/baidu/uid-generator)版本保持一致

## 参数说明

|参数|名称|默认值|备注|
|---|---|---|---|
|**sensin.uid.timeBits**|时间长度|30|可使用时长为以时间基点epochStr为起点，timeBits为增量的时间长度|
|**sensin.uid.workerBits**|机器id|18|最多可支持约2^n次机器启动|
|**sensin.uid.seqBits**|每秒下的并发序列|15|该值越大，每秒支持的并发生成的序列越大|
|**sensin.uid.epochStr**|时间基点|2019-07-10|格式：yyyy-MM-dd，这个值会转换成毫秒的时间戳，用于时间长度起始|
|**sensin.uid.boostPower**|RingBuffer size扩容参数|3|可提高UID生成的吞吐量，过高会造成栈溢出|
|**sensin.uid.paddingFactor**|RingBuffer填充百分比|65|该值在(0,100)范围内，当RingBuffer内值到达该百分比时，自动填充满RingBuffer|
|**sensin.uid.scheduleInterval**|RingBuffer填充周期|2019-07-10|另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充。一般不使用这个属性，除非使用ID的频次固定。|


## WorkerID生成策略

默认使用com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner来进行生成，可自定义，需要符合算法规范。

## 拒绝策略

* 当环已满, 无法继续填充时

    默认无需指定, 将丢弃Put操作, 仅日志记录。
    
    如有特殊需求, 请实现RejectedPutBufferHandler接口。
    
* 当环已空, 无法继续获取时

    默认无需指定, 将记录日志, 并抛出UidGenerateException异常。
    
    如有特殊需求, 请实现RejectedTakeBufferHandler接口。
