package dit.anonymous.webapp.controllers.gi;

import dit.anonymous.webapp.execution.gi.InterlinkingManager;
import dit.anonymous.webapp.execution.gi.InterlinkingResults;
import dit.anonymous.webapp.utilities.configurations.HttpPaths;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(HttpPaths.interlinking + "**")
public class InterlinkingController {

    @Autowired
    private HttpServletRequest request;

    @PostMapping(path = HttpPaths.giReadData + "setConfigurationWithFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String setDatasetWithFile(@RequestPart(value="file") MultipartFile file, @RequestPart String json_conf){
        JSONObject configurations = new JSONObject(json_conf);
        String source = UploadFile(file);
        return InterlinkingManager.setDataset(configurations, source);
    }

    /**
     * Upload the input file in the server
     * @param file the input file
     * @return the path
     **/
    public String UploadFile(MultipartFile file) {
        String realPathToUploads = request.getServletContext().getRealPath("/uploads/");
        if(! new File(realPathToUploads).exists())
            new File(realPathToUploads).mkdir();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String filepath = realPathToUploads + filename;
        File dest = new File(filepath);
        if(! dest.exists()) {
            try {
                file.transferTo(dest);
                System.out.println("File was stored Successfully in "+ filepath);
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("File already exist in "+ filepath);
        }
        return filepath;
    }


    @GetMapping(HttpPaths.setInterlinking + "{algType}/{algorithm}/{budget}")
    public void setInterlinking(@PathVariable(value = "algType") String algType,
                                @PathVariable(value = "algorithm") String algorithm,
                                @PathVariable(value = "budget") int budget) {
            InterlinkingManager.setAlgorithm(algorithm);
            InterlinkingManager.setBudget(budget);
            InterlinkingManager.setAlgorithmType(algType);
    }


    @GetMapping(HttpPaths.interlinking + "run")
    public InterlinkingResults run() {
        System.out.println("Interlinker Run");
        return InterlinkingManager.run();
    }

    @GetMapping(HttpPaths.interlinking + "getAlgorithmConf/{algorithm}")
    public String getConfiguration(@PathVariable(value = "algorithm") String algorithm) {
        System.out.println("get configurations of " + algorithm);
        return InterlinkingManager.getConfiguration(algorithm);
    }

    @GetMapping(HttpPaths.interlinking + "getExecutions")
    public List<InterlinkingResults> getExecutions() {
        return InterlinkingManager.results;
    }
}
