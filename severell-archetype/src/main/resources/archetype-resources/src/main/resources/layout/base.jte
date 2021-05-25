@import gg.jte.Content

@param Content content
@param Content scripts
@param Content header
@param String title


<html>
    <head>
        <title>${title}</title>
        <link rel="stylesheet" href="/static/css/main.css">
        <link rel="shortcut icon" href="/static/images/favicon.ico">
        ${header}
    </head>
    <body class="antialiased font-sans bg-gray-200">
        <div class="content">
            ${content}
        </div>

        <script src="/static/js/app.js"></script>
        ${scripts}
    </body>
</html>