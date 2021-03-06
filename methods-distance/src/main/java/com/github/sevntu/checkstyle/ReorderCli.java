package com.github.sevntu.checkstyle;

import com.github.sevntu.checkstyle.domain.Dependencies;
import com.github.sevntu.checkstyle.module.DependencyInformationConsumer;
import com.github.sevntu.checkstyle.common.MethodCallDependencyCheckInvoker;
import com.github.sevntu.checkstyle.ordering.MethodOrder;
import com.github.sevntu.checkstyle.reordering.TopologicalMethodReorderer;
import com.github.sevntu.checkstyle.utils.FileUtils;
import com.github.sevntu.checkstyle.dsm.DependencyInfoMatrixSerializer;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Application entry point that accepts file path and generates
 * DSM for initial methods ordering of class and DSM for method
 * ordering generated by topological sorting algorithm.
 */
public final class ReorderCli {

    private ReorderCli() { }

    public static void main(String... args) throws CheckstyleException {

        final Map<String, String> attributes = Collections.singletonMap("screenLinesCount", "50");

        final DependencyInformationSerializer serializer = new DependencyInformationSerializer();

        final MethodCallDependencyCheckInvoker runner =
            new MethodCallDependencyCheckInvoker(attributes, serializer);

        final List<File> files = Collections.singletonList(new File(args[0]));
        runner.invoke(files);
    }

    private static final class DependencyInformationSerializer implements
        DependencyInformationConsumer {

        private Configuration configuration;

        private DependencyInformationSerializer() { }

        @Override
        public void setConfiguration(Configuration config) {
            this.configuration = config;
        }

        @Override
        public void accept(String filePath, Dependencies dependencies) {
            final String baseName = new File(filePath).getName();
            final String source = FileUtils.getFileContents(filePath);
            final MethodOrder initialMethodOrder = new MethodOrder(dependencies);
            final MethodOrder topologicalMethodOrder = new TopologicalMethodReorderer()
                .reorder(initialMethodOrder);
            DependencyInfoMatrixSerializer.writeToFile(
                source, initialMethodOrder, configuration, baseName + ".initial.html");
            DependencyInfoMatrixSerializer.writeToFile(
                source, topologicalMethodOrder, configuration, baseName + ".topological.html");
        }
    }
}
