package com.curtisnewbie.gateway.constants;

/**
 * Predefined paths
 *
 * @author yongj.zhuang
 */
public final class DefinedPaths {
    private DefinedPaths(){

    }

    /*
     * user-related
     */
    /** user info */
    public static final String INFO_PATH = "/auth-service/open/api/user/info";
    /** login */
    public static final String LOGIN_PATH = "/auth-service/open/api/user/login";
    /** registration */
    public static final String REG_PATH = "/auth-service/open/api/user/register/request";

    /*
    file-related
     */
    /** media streaming from file-service */
    public static final String MEDIA_STREAMING_PATH = "/file-service/open/api/file/token/media/streaming";
    /** download file from file-service */
    public static final String FILE_DOWNLOAD_PATH = "/file-service/open/api/file/token/download";
    /** download file from fantahsea */
    public static final String FANTAHSEA_DOWNLOAD_PATH = "/fantahsea/open/api/gallery/image/download";
}
