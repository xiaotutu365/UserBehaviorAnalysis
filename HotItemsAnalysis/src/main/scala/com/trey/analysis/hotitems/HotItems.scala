package com.trey.analysis.hotitems

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object HotItems {
  def main(args: Array[String]): Unit = {
    // 1.创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    // 2.读取数据
    val dataStream = env.readTextFile("E:\\workspace\\idea\\git\\UserBehaviorAnalysis\\HotItemsAnalysis\\src\\main\\resources\\UserBehavior.csv")
      .map(data => {
        val dataArray = data.split(",")
        UserBehavior(dataArray(0).trim.toLong, dataArray(1).trim.toLong, dataArray(2).trim.toInt, dataArray(3).trim, dataArray(4).trim.toLong)
      }).assignAscendingTimestamps(_.timestamp * 1000)

    // 3.处理数据
    val processedStream = dataStream
        .filter(_.behavior == "pv")
        .keyBy("itemId")
        .timeWindow(Time.hours(1), Time.minutes(5))
        .aggregate(new CountAgg(), new WindowResult())

    // 4.控制台输出
    dataStream.print()

    env.execute("hot items job")
  }
}
