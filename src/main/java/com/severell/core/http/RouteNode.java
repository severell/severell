package com.severell.core.http;

import java.util.ArrayList;
import java.util.Arrays;

public class RouteNode {

    public enum NodeType {
        Root,
        CatchAll,
        Param,
        Static
    }

    private class WildCardReturn {
        private String wildcard;
        private boolean valid;
        private int index;

        private WildCardReturn(String wildcard, int index, boolean valid) {
            this.wildcard = wildcard;
            this.index = index;
            this.valid = valid;
        }

        private String getWildcard() {
            return wildcard;
        }

        private boolean isValid() {
            return valid;
        }

        private int getIndex() {
            return index;
        }

    }

    private String path;
    private ArrayList<RouteNode> children;
    private RouteExecutor handle;
    private boolean isWildCard;
    private NodeType type;

    protected RouteNode() {
        children = new ArrayList<RouteNode>();
    }

    protected ArrayList<RouteNode> getChildren() {
        return this.children;
    }

    protected String getPath() {
        return this.path;
    }

    protected RouteExecutor getHandle() {
        return this.handle;
    }

    protected boolean isWildCard() {
        return isWildCard;
    }

    protected NodeType getType() {
        return this.type;
    }

    protected void insert(String path, RouteExecutor handle) throws Exception {
        String fullPath = path;
        RouteNode traverseNode = this;

        //is First Route
        if(traverseNode.path == null && children.size() == 0) {
            insertChild(path, fullPath, handle, traverseNode);
            return;
        }

        walk:
        for (;;) {
            int i = findLongestCommonPrefix(path, traverseNode.path);

            //Need to split the edge
            if(i < traverseNode.path.length()) {
                RouteNode newNode = new RouteNode();
                newNode.children = traverseNode.getChildren();
                newNode.handle = traverseNode.handle;
                newNode.path = traverseNode.path.substring(i);
                newNode.isWildCard = traverseNode.isWildCard();
                newNode.type = NodeType.Static;

                traverseNode.children = new ArrayList<RouteNode>(Arrays.asList(new RouteNode[]{newNode}));
                traverseNode.path = path.substring(0,i);
                traverseNode.handle= null;
                traverseNode.isWildCard = false;
            }

            if(i < path.length()) {
                path = path.substring(i);

                if(traverseNode.isWildCard()){
                    traverseNode = traverseNode.getChildren().get(0);

//                    if len(path) >= len(n.path) && n.path == path[:len(n.path)] &&
//                    // Adding a child to a catchAll is not possible
//                    n.nType != catchAll &&
//                            // Check for longer wildcard, e.g. :name and :names
//                            (len(n.path) >= len(path) || path[len(n.path)] == '/') {

                    boolean pathIsGreaterThanOrEqualToNodePath = (path.length() >= traverseNode.getPath().length() && traverseNode.getPath().equals(path.substring(0,traverseNode.getPath().length())));
                    boolean isNotCatchAll = traverseNode.getType() != NodeType.CatchAll;
                    boolean checkForLongerNames = (traverseNode.getPath().length() >= path.length() || path.charAt(traverseNode.getPath().length()) == '/');

                    if( pathIsGreaterThanOrEqualToNodePath
                            && isNotCatchAll && checkForLongerNames) {
                        continue walk;
                    } else {
                        throw new Exception("Whoa, Conflict Much");
                    }
                }

                char firstChar = path.charAt(0);
                // '/' after param
                if(traverseNode.getType() == NodeType.Param && firstChar == '/' && traverseNode.getChildren().size() == 1) {
                    traverseNode = traverseNode.getChildren().get(0);
                    continue walk;
                }

                //Check if any children exist that we can go to next
                for(RouteNode child : traverseNode.getChildren()) {
                    int j = findLongestCommonPrefix(path, child.path);
                    if(j > 0) {
                        traverseNode = child;
                        continue walk;
                    }
                }

                //Otherwise insert child
                if(firstChar != ':' && firstChar != '*') {
                    RouteNode newChild = new RouteNode();
                    newChild.path = path;
                    newChild.handle = handle;
                    RouteNode oldNode = traverseNode;
                    traverseNode.getChildren().add(newChild);
                    traverseNode = newChild;
                    insertChild(path, fullPath, handle, traverseNode);


                } else {
                    insertChild(path, fullPath, handle, traverseNode);
                }
                return;
            } else {
                traverseNode.handle = handle;
                return;
            }
        }
    }

    private WildCardReturn findWildcard(String path) {
        int start = 0;
        for(char c : path.toCharArray()) {

            // A wildcard starts with ':' (param) or '*' (catch-all)
            if(c != ':' && c != '*') {
                start++;
                continue;
            }

            // Find end and check for invalid characters
            boolean valid = true;
            int end = 0;
            for(char ec : path.substring(start+1).toCharArray()) {
                switch (ec) {
                    case '/':
                        return new WildCardReturn(path.substring(start , start+1+end), start, valid);
                    case ':':
                    case '*':
                        valid = false;
                        break;
                }
                end++;
            }
            return new WildCardReturn(path.substring(start), start, valid);
	    }
        return new WildCardReturn("", -1, false);
    }

    private void insertChild(String path, String fullPath, RouteExecutor handle, RouteNode traverseNode) throws Exception {
        for(;;) {
            WildCardReturn ret = findWildcard(path);
            if (ret.getIndex() < 0) { // No wildcard found
                break;
            }

            if(!ret.isValid()) {
                throw new Exception("Only one wildcard per path segment is allowed");
            }

            if(ret.getWildcard().length() < 2) {
                throw new Exception("wildcards must be named with a non-empty name in path '" + fullPath + "'");
            }

            if(traverseNode.getChildren().size() > 0) {
                throw new Exception("wildcard segment '" + ret.getWildcard() +
                       "' conflicts with existing children in path '" + fullPath + "'");
            }

            if(ret.getWildcard().charAt(0) == ':') {
                if (ret.getIndex() > 0) {
                    traverseNode.path = path.substring(0, ret.getIndex());
                    path = path.substring(ret.getIndex());
                }
                traverseNode.isWildCard = true;
                RouteNode newChild = new RouteNode();
                newChild.type = NodeType.Param;
                newChild.path = ret.getWildcard();
                traverseNode.children = new ArrayList<RouteNode>(Arrays.asList(new RouteNode[]{newChild}));
                traverseNode = newChild;

                // If the path doesn't end with the wildcard, then there
                // will be another non-wildcard subpath starting with '/'
                if (ret.getWildcard().length() < path.length()) {
                    path = path.substring(ret.getWildcard().length());
                    RouteNode child = new RouteNode();
                    traverseNode.children = new ArrayList<RouteNode>(Arrays.asList(new RouteNode[]{child}));
                    traverseNode = child;
                    continue;
                }

                traverseNode.handle = handle;
                return;
            } else {

                if(ret.getIndex()+ret.getWildcard().length() != path.length()) {
                    throw new Exception("catch-all routes are only allowed at the end of the path in path '" + fullPath + "'");
                }

                if((traverseNode.getPath() != null && traverseNode.getPath().length() > 0) && traverseNode.getPath().substring(traverseNode.getPath().length()-1).equals("/")) {
                    throw new Exception("catch-all conflicts with existing handle for the path segment root in path '" + fullPath + "'");
                }

                // Currently fixed width 1 for '/'
                ret.index = ret.getIndex() - 1;
                if(path.charAt(ret.getIndex()) != '/') {
                    throw new Exception("no / before catch-all in path '" + fullPath + "'");
                }

                traverseNode.path = path.substring(0, ret.getIndex());
                RouteNode newChild = new RouteNode();
                newChild.isWildCard = true;
                newChild.type = NodeType.CatchAll;
                newChild.path = "/";
                traverseNode.children = new ArrayList<RouteNode>(Arrays.asList(new RouteNode[]{newChild}));
                traverseNode = newChild;

                // Second node: node holding the variable
                RouteNode secChild = new RouteNode();
                secChild.type = NodeType.CatchAll;
                secChild.path = path.substring(ret.getIndex());
                secChild.handle = handle;
                traverseNode.children = new ArrayList<RouteNode>(Arrays.asList(new RouteNode[]{secChild}));

                return;

            }

        }

        // If no wildcard was found, simply insert the path and handle
        traverseNode.path = path;
        traverseNode.handle = handle;
        return;
    }

    private int findLongestCommonPrefix(String a, String b) {
        int i  = 0;
        int max = Math.min(a.length(), b.length());
        while(i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }


}
