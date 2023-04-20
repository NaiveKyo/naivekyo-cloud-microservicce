package io.naivekyo.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.function.Function;

/**
 * <p>
 *     JDK 8 time api support.
 * </p>
 * @author NaiveKyo
 * @since 1.0
 */
public class DateUtils {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 获取当天日期所属年份
	 * @return year 年份数值
	 */
	public static Integer getYear() {
		return LocalDate.now().getYear();
	}

	/**
	 * 获取当天日期所属月份
	 * @return month 数值类型 1 - 12
	 */
	public static Integer getMonth() {
		return LocalDate.now().getMonthValue();
	}

	// ================================ format LocalDate ========================

	/**
	 * 获取当天日期字符串
	 * 默认格式: yyyy-MM-dd
	 * @throws java.time.DateTimeException 格式化失败则抛出异常
	 */
	public static String dateFormat() {
		return LocalDate.now().format(getFormatter(DEFAULT_DATE_FORMAT));
	}

	/**
	 * 以给定的模式格式化当天日期
	 * @param formatStr 格式字符串
	 * @throws java.time.DateTimeException 格式化失败则抛出异常  
	 */
	public static String dateFormat(String formatStr) {
		return LocalDate.now().format(getFormatter(formatStr));
	}

	/**
	 * 格式化指定的日期变量
	 * @param date 			日期对象 {@link LocalDate}
	 * @param formatStr		格式字符串
	 * @throws java.time.DateTimeException 格式化失败则抛出异常   
	 * @return				字符串
	 */
	public static String dateFormat(LocalDate date, String formatStr) {
		return date.format(getFormatter(formatStr));
	}

	/**
	 * <p>
	 *     以默认格式来格式化给定的日期对象
	 *     默认格式: yyyy-MM-dd
	 * </p>
	 * @param date	指定的日期对象{@link LocalDate}
	 * @throws java.time.DateTimeException 格式化失败则抛出异常   
	 * @return		字符串
	 */
	public static String dateFormat(LocalDate date) {
		return date.format(getFormatter(DEFAULT_DATE_FORMAT));
	}

	// ================================ parse LocalDate ========================

	/**
	 * 默认格式解析时间字符串
	 * @param date 时间字符串 默认格式 {@link #DEFAULT_DATE_FORMAT}
	 * @throws java.time.format.DateTimeParseException 解析异常
	 * @return {@link LocalDate}
	 */
	public static LocalDate parseLocalDate(String date) {
		return parseLocalDate(date, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 按照指定格式解析时间字符串
	 * @param date 		时间字符串
	 * @param pattern	格式字符串 默认格式 {@link #DEFAULT_DATE_FORMAT}
	 * @throws java.time.format.DateTimeParseException 解析异常
	 * @return {@link LocalDate}
	 */
	public static LocalDate parseLocalDate(String date, String pattern) {
		return LocalDate.parse(date, getFormatter(pattern));
	}

	// ================================ format LocalDateTime ========================

	/**
	 * 获取当前时间字符串
	 * 默认格式: yyyy-MM-dd HH:mm:ss
	 * @throws java.time.DateTimeException 时间格式化异常
	 */
	public static String dateTimeFormat() {
		return LocalDateTime.now().format(getFormatter(DEFAULT_DATETIME_FORMAT));
	}

	/**
	 * 以给定的模式格式化当前时间
	 * @param formatStr 格式字符串
	 * @throws java.time.DateTimeException 时间格式化异常   
	 */
	public static String dateTimeFormat(String formatStr) {
		return LocalDateTime.now().format(getFormatter(formatStr));
	}

	/**
	 * 将指定的时间按照默认格式进行格式化
	 * 默认格式: yyyy-MM-dd HH:mm:ss
	 * @param dateTime 时间
	 * @throws java.time.DateTimeException 时间格式化异常   
	 */
	public static String dateTimeFormat(LocalDateTime dateTime) {
		return getFormatter(DEFAULT_DATETIME_FORMAT).format(dateTime);
	}

	/**
	 * 将指定的时间按照指定格式进行格式化
	 *
	 * @param dateTime	时间
	 * @param pattern	格式
	 * @throws java.time.DateTimeException 时间格式化异常   
	 */
	public static String dateTimeFormat(LocalDateTime dateTime, String pattern) {
		return getFormatter(pattern).format(dateTime);
	}

	// ================================ parse LocalDateTime ========================

	/**
	 * 默认格式解析时间字符串
	 * @param date 时间字符串 默认格式 {@link #DEFAULT_DATETIME_FORMAT}
	 * @return {@link LocalDateTime}
	 * @throws java.time.format.DateTimeParseException 解析异常
	 */
	public static LocalDateTime parseLocalDateTime(String date) {
		return parseLocalDateTime(date, DEFAULT_DATETIME_FORMAT);
	}

	/**
	 * 按照指定格式解析时间字符串
	 * @param date 		时间字符串
	 * @param pattern	格式字符串 默认格式 {@link #DEFAULT_DATETIME_FORMAT}
	 * @return {@link LocalDateTime}
	 * @throws java.time.format.DateTimeParseException 解析异常
	 */
	public static LocalDateTime parseLocalDateTime(String date, String pattern) {
		return LocalDateTime.parse(date, getFormatter(pattern));
	}

	// ================================ generic format ========================

	/**
	 * 将字符串转换为时间实例, 转换方法自定义, 注意可能导致的异常
	 *
	 * @param str		目标字符串
	 * @param function	转换方法
	 * @param <R>		目标时间类型
	 * @return			时间对象实例
	 * @throws java.time.format.DateTimeParseException 解析异常
	 */
	public static <R> R stringToTime(String str, Function<String, R> function) {
		return function.apply(str);
	}

	// ================================ legacy date transform ====================

	/**
	 * 将 {@link Date} 转换为 {@link LocalDateTime}
	 *
	 * @param date {@link Date}
	 * @return {@link LocalDateTime}
	 */
	public static LocalDateTime convertDateToLocalDateTime(Date date) {
		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * 将 {@link Date} 转化为 {@link LocalDate}
	 * @param date {@link Date}
	 * @return {@link LocalDate}
	 */
	public static LocalDate convertDateToLocalDate(Date date) {
		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * 获得特定格式的时间格式化器
	 *
	 * @param formatStr 格式字符串
	 * @return	{@link DateTimeFormatter}
	 */
	private static DateTimeFormatter getFormatter(String formatStr) {
		return DateTimeFormatter.ofPattern(formatStr);
	}
	
	// ====================================== time arithmetic operation ============================

	/**
	 * <p>计算两个时间相差的天数, 注意可能抛出的异常</p>
	 * <p>如果传入的参数是 LocalDate 类型的, 则正常的计算天数, 比如 2022-11-02 和 2022-11-03 相差一天, 返回 1;</p>
	 * <p>如果传入的参数是 LocalDateTime 类型的, 则计算天数时考虑时间参数, 比如 2022-11-02 10:00:00 和 2022-11-03 09:00:00 没有相差天数, 返回 0, 表示属于同一天;</p>
	 * <p>注意区间: [begin, end)</p>
	 * @param begin	起始时间, 包含
	 * @param end	终止时间, 不包含
	 * @return 整数值:
	 * <ul>
	 *     <li>0 表示属于同一天内</li>
	 *     <li>正数 表示起始时间早于终止时间</li>
	 *     <li>负数 表示起始时间晚于终止时间(应该避免出现此种情况)</li>
	 * </ul>
	 * @deprecated 不推荐使用, 推荐使用 {@link #between(Temporal, Temporal, TemporalUnit)}
	 * @see #between(Temporal, Temporal, TemporalUnit) 
	 */
	@Deprecated
	public static long betweenDays(Temporal begin, Temporal end) {
		return ChronoUnit.DAYS.between(begin, end);
	}

	/**
	 * <p>计算两个时间的间距, 注意可能抛出的异常</p>
	 * <p>以计算两个时间点相差的天数为例</p>
	 * <ul>
	 *     <li>如果传入的参数是 LocalDate 类型的, 则正常的计算天数, 比如 2022-11-02 和 2022-11-03 相差一天, 返回 1;</li>
	 *     <li>如果传入的参数是 LocalDateTime 类型的, 则计算天数时考虑时间参数, 比如 2022-11-02 10:00:00 和 2022-11-03 09:00:00 没有相差天数, 返回 0, 表示属于同一天;</li>
	 * </ul>
	 * <p>注意区间: [begin, end)</p>
	 * @param begin	起始时间, 包含
	 * @param end	终止时间, 不包含
	 * @param unit 相差时间的单位, 如 {@link ChronoUnit#DAYS}
	 * @return 整数值, 大于等于 0
	 * @throws IllegalArgumentException 非法参数异常, 起始时间必须早于终止时间
	 */
	public static long between(Temporal begin, Temporal end, TemporalUnit unit) {
		long between = unit.between(begin, end);
		if (between < 0)
			throw new IllegalArgumentException("参数 begin 代表的时间必须在 end 时间之前");
		return between;
	}
}
