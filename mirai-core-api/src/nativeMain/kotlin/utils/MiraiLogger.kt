/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.reflect.KClass

/**
 * 日志记录器.
 *
 * ## Mirai 日志系统
 *
 * Mirai 内建简单的日志系统, 即 [MiraiLogger]. [MiraiLogger] 的实现有 [SimpleLogger], [PlatformLogger], [SilentLogger].
 *
 * [MiraiLogger] 仅能处理简单的日志任务, 通常推荐使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 等日志库.
 *
 * ## 使用第三方日志库接管 Mirai 日志系统
 *
 * 使用 [LoggerAdapters], 将第三方日志 `Logger` 转为 [MiraiLogger]. 然后通过 [MiraiLogger.setDefaultLoggerCreator] 全局覆盖日志.
 *
 * ## 实现或使用 [MiraiLogger]
 *
 * 不建议实现或使用 [MiraiLogger]. 请优先考虑使用上述第三方框架. [MiraiLogger] 仅应用于兼容旧版本代码.
 *
 * @see SimpleLogger 简易 logger, 它将所有的日志记录操作都转移给 lambda `(String?, Throwable?) -> Unit`
 * @see PlatformLogger 各个平台下的默认日志记录实现.
 * @see SilentLogger 忽略任何日志记录操作的 logger 实例.
 * @see LoggerAdapters
 *
 * @see MiraiLoggerPlatformBase 平台通用基础实现. 若 Mirai 自带的日志系统无法满足需求, 请继承这个类并实现其抽象函数.
 */
public actual interface MiraiLogger {
    /**
     * 可以 service 实现的方式覆盖.
     *
     * @since 2.7
     */
    public actual interface Factory {
        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象的 class
         * @param identity 对象标记 (备注)
         */
        public actual fun create(requester: KClass<*>, identity: String?): MiraiLogger = TODO("native")

        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象
         */
        public actual fun create(requester: KClass<*>): MiraiLogger = TODO("native")

        public actual companion object INSTANCE : Factory by TODO("native")
    }

    public actual companion object {
        /**
         * 顶层日志, 仅供 Mirai 内部使用.
         */
        @MiraiInternalApi
        @MiraiExperimentalApi
        @Deprecated("Deprecated.", level = DeprecationLevel.HIDDEN) // deprecated since 2.7
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
        public actual val TopLevel: MiraiLogger by lazy { Factory.create(MiraiLogger::class, "Mirai") }

        /**
         * 已弃用, 请实现 service [net.mamoe.mirai.utils.MiraiLogger.Factory] 并以 [ServiceLoader] 支持的方式提供.
         */
        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(
            "Please set factory by providing an service of type net.mamoe.mirai.utils.MiraiLogger.Factory",
            level = DeprecationLevel.ERROR
        ) // deprecated since 2.7
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10") // left ERROR intentionally, for internal uses.
        public actual fun setDefaultLoggerCreator(creator: (identity: String?) -> MiraiLogger) {
            throw UnsupportedOperationException()
        }

        /**
         * 旧版本用于创建 [MiraiLogger]. 已弃用. 请使用 [MiraiLogger.Factory.INSTANCE.create].
         *
         * @see setDefaultLoggerCreator
         */
        @Deprecated(
            "Please use MiraiLogger.Factory.create", ReplaceWith(
                "MiraiLogger.Factory.create(YourClass::class, identity)",
                "net.mamoe.mirai.utils.MiraiLogger"
            ), level = DeprecationLevel.HIDDEN
        ) // deprecated since 2.7
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
        public actual fun create(identity: String?): MiraiLogger = Factory.create(MiraiLogger::class, identity)
    }

    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     *
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    public actual val identity: String?

    /**
     * 获取 [MiraiLogger] 是否已开启
     *
     * 除 [MiraiLoggerWithSwitch] 可控制开关外, 其他的所有 [MiraiLogger] 均一直开启.
     */
    public actual val isEnabled: Boolean

    /**
     * 当 VERBOSE 级别的日志启用时返回 `true`.
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 在使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 时返回真实配置值.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isVerboseEnabled: Boolean get() = isEnabled

    /**
     * 当 DEBUG 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 在使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 时返回真实配置值.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isDebugEnabled: Boolean get() = isEnabled

    /**
     * 当 INFO 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 在使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 时返回真实配置值.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isInfoEnabled: Boolean get() = isEnabled

    /**
     * 当 WARNING 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 在使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 时返回真实配置值.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isWarningEnabled: Boolean get() = isEnabled

    /**
     * 当 ERROR 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 在使用 [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 时返回真实配置值.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public actual val isErrorEnabled: Boolean get() = isEnabled

    /**
     * 随从. 在 this 中调用所有方法后都应继续往 [follower] 传递调用.
     * [follower] 的存在可以让一次日志被多个日志记录器记录.
     *
     * 一般不建议直接修改这个属性. 请通过 [plus] 来连接两个日志记录器.
     * 如: `val logger = bot.logger + MyLogger()`
     * 当调用 `logger.info()` 时, `bot.logger` 会首先记录, `MyLogger` 会随后记录.
     *
     * 当然, 多个 logger 也可以加在一起: `val logger = bot.logger + MynLogger() + MyLogger2()`
     */
    @Suppress("UNUSED_PARAMETER")
    @Deprecated("follower 设计不佳, 请避免使用", level = DeprecationLevel.HIDDEN) // deprecated since 2.7
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
    public actual var follower: MiraiLogger?
        get() = null
        set(value) {}

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    public actual fun verbose(message: String?)

    public actual fun verbose(e: Throwable?): Unit = verbose(null, e)
    public actual fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    public actual fun debug(message: String?)

    public actual fun debug(e: Throwable?): Unit = debug(null, e)
    public actual fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 _信息_ 级别的日志.
     */
    public actual fun info(message: String?)

    public actual fun info(e: Throwable?): Unit = info(null, e)
    public actual fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 _警告_ 级别的日志.
     */
    public actual fun warning(message: String?)

    public actual fun warning(e: Throwable?): Unit = warning(null, e)
    public actual fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 _错误_ 级别的日志.
     */
    public actual fun error(message: String?)

    public actual fun error(e: Throwable?): Unit = error(null, e)
    public actual fun error(message: String?, e: Throwable?)

    /** 根据优先级调用对应函数 */
    public actual fun call(priority: SimpleLogger.LogPriority, message: String?, e: Throwable?): Unit =
        priority.correspondingFunction(this, message, e)

    /**
     * 添加一个 [follower], 返回 [follower]
     * 它只会把 `this` 的属性 [MiraiLogger.follower] 修改为这个函数的参数 [follower], 然后返回这个参数.
     * 若 [MiraiLogger.follower] 已经有值, 则会替换掉这个值.
     * ```
     *   +------+      +----------+      +----------+      +----------+
     *   | base | <--  | follower | <--  | follower | <--  | follower |
     *   +------+      +----------+      +----------+      +----------+
     * ```
     *
     * @return [follower]
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("plus 设计不佳, 请避免使用.", level = DeprecationLevel.HIDDEN) // deprecated since 2.7
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
    public actual operator fun <T : MiraiLogger> plus(follower: T): T = follower
}
