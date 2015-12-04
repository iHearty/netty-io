package cn.togeek.netty.util;

/**
 * Generates opaque unique strings.
 */
interface UUIDGenerator {
   public String getBase64UUID();
}