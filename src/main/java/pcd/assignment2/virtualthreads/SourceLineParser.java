package pcd.assignment2.virtualthreads;

public class SourceLineParser {
    private boolean parsingMultipleLinesBlockComment;

    public SourceLineParser() {
        this.parsingMultipleLinesBlockComment = false;
    }

    public boolean parseLine(String line) {
        line = line.trim();

        if (!line.isEmpty()) {

            if (!parsingMultipleLinesBlockComment) {

                // check for block comments
                int indexStartComment = line.indexOf("/*");

                if (indexStartComment == -1 ) {
                    // no block comments

                    // check for EoL comments
                    indexStartComment = line.indexOf("//");
                    if (indexStartComment == -1) {
                        // no comments
                        return true;
                    } else {
                        // is there code before the comment?
                        String before = line.substring(0, indexStartComment).trim();
                        if (!before.isEmpty()) {
                            // line with start comment and code
                            return true;
                        }
                    }
                } else {
                    // a block comment started

                    // is there code before the comment?
                    String before = line.substring(0, indexStartComment).trim();
                    if (!before.isEmpty()) {
                        // line with start comment and code
                        return true;
                    }

                    // check if the block comment ends with within the line
                    int indexEndComment = line.indexOf("*/");
                    if (indexEndComment == -1) {

                        // block comment started, multiple lines

                        parsingMultipleLinesBlockComment = true;
                    }

                }
            } else {
                // skipping all lines

                int commentEndIndex = line.indexOf("*/");
                if (commentEndIndex != -1) {
                    parsingMultipleLinesBlockComment = false;
                    if (commentEndIndex < line.length() - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
