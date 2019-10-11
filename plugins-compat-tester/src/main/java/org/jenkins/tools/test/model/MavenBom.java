package org.jenkins.tools.test.model;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MavenBom {

    private Model contents;

    public MavenBom(File path) throws IOException, XmlPullParserException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            contents = reader.read(in);
            Model m = new Model();
        }
    }

    public Model getModel() {
        return contents;
    }

    public File writeFullDepPom(File workDir) throws IOException {
        Model modified = getModel().clone();
        File fullDepPom = new File(workDir, "bom/pom.xml");
        FileUtils.forceMkdir(fullDepPom.getParentFile());
        List<Dependency> managed = getModel().getDependencyManagement().getDependencies();
        for (Dependency dep : managed) {
            if (!this.containsDep(dep, modified)) {
                modified.addDependency(dep);
            }
        }
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try (FileOutputStream out = new FileOutputStream(fullDepPom)) {
            writer.write(out, modified);
            return fullDepPom;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean containsDep(Dependency toCheck, Model model) {
        return model.getDependencies().stream().filter(dep -> getDepDescription(dep).equals(getDepDescription(toCheck))).count() > 0;
    }

    private String getDepDescription(Dependency toCheck) {
        return String.join(":", toCheck.getGroupId(),toCheck.getArtifactId(),toCheck.getVersion(),toCheck.getType(),toCheck.getClassifier(),toCheck.getScope());
    }
}
