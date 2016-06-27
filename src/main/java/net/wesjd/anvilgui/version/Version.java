package net.wesjd.anvilgui.version;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.wesjd.anvilgui.version.impl.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 BuildStatic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public enum Version {

    ONE_EIGHT_R1("1_8_R1", Wrapper1_8_R1.class),
    ONE_EIGHT_R2("1_8_R2", Wrapper1_8_R2.class),
    ONE_EIGHT_R3("1_8_R3", Wrapper1_8_R3.class),
    ONE_NINE_R1("1_9_R1", Wrapper1_9_R1.class),
    ONE_NINE_R2("1_9_R2", Wrapper1_9_R2.class);

    private static final LoadingCache<Class<? extends VersionWrapper>, VersionWrapper> WRAPPER_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(values().length)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build(new CacheLoader<Class<? extends VersionWrapper>, VersionWrapper>() {
                        @Override
                        public VersionWrapper load(Class<? extends VersionWrapper> aClass) throws Exception {
                            return aClass.newInstance();
                        }
                    });

    private final String pkg;
    private final Class<? extends VersionWrapper> wrapper;

    Version(String pkg, Class<? extends VersionWrapper> wrapper) {
        this.pkg = pkg;
        this.wrapper = wrapper;
    }

    public String getPkg() {
        return pkg;
    }

    public VersionWrapper getWrapper() {
        try {
            return WRAPPER_CACHE.get(wrapper);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Version of(final String pkg) {
        for(Version version : values()) if(pkg.equals("v" + version.getPkg())) return version;
        return null;
    }

}
