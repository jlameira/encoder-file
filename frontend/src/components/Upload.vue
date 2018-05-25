<template>
  <div>
    <h1>Upload File</h1>
    <div class="dropbox">
      <input type="file" multiple :name="uploadFieldName" :disabled="isSaving" @change="filesChange($event.target.name, $event.target.files); fileCount = $event.target.files.length"
        accept="*/*" class="input-file">
      <p v-if="isInitial">
        Drag your file(s) here to begin
        <br> or click to browse
      </p>
      <p v-if="isSaving">
        Uploading {{ fileCount }} files...
      </p>
      <!-- <pacman-loader :loading="loading" :color="color"></pacman-loader> -->

    </div>
    <div class="center">

      <ring-loader :loading="loading" :color="color"></ring-loader>
      <!-- <clip-loader :loading="loading"></clip-loader> -->
    </div>

    <!--SUCCESS-->
    <div v-if="isSuccess">
      <h2>Uploaded {{ uploadedFiles.length }} file(s) successfully.</h2>
      <p>
        <a href="javascript:void(0)" @click="reset()">Upload again</a>
      </p>
      <div>
        <video name="media" controls autoplay="autoplay">
          <source :src="source" type="video/mp4" />
        </video>
      </div>
    </div>
    <!--FAILED-->
    <div v-if="isFailed">
      <h2>Uploaded failed.</h2>
      <p>
        <a href="javascript:void(0)" @click="reset()">Try again</a>
      </p>
      <pre>{{ uploadError }}</pre>
    </div>
  </div>

</template>

<script>
  /* eslint-disable */

  import Vue from 'vue'
  import FileUpload from 'v-file-upload'
  import axios from 'axios'
  import {
    wait
  } from '../util/util.js';
  import ClipLoader from "vue-spinner/src/ClipLoader.vue";
  import PacmanLoader from "vue-spinner/src/PacmanLoader.vue";
  import RingLoader from "vue-spinner/src/RingLoader.vue";


  Vue.use(FileUpload)
  const STATUS_INITIAL = 0,
    STATUS_SAVING = 1,
    STATUS_SUCCESS = 2,
    STATUS_FAILED = 3;
  export default {
    name: 'HelloWorld',
    data() {
      return {
        url: '/api/upload',
        headers: {},
        filesUploaded: [],
        uploadedFiles: [],
        uploadError: null,
        currentStatus: null,
        uploadFieldName: 'video',
        source: "",
        loading: false,
        color: '#5599b5'
      }
    },
    methods: {
      thumbUrl(file) {
        return file.myThumbUrlProperty
      },
      onFileChange(file) {
        this.fileUploaded = file
        console.log(this.filesUploaded)
      },
      reset() {
        // reset form to initial state
        this.currentStatus = STATUS_INITIAL;
        this.uploadedFiles = [];
        this.uploadError = null;
      },
      save(formData) {
        // upload data to the server
        this.currentStatus = STATUS_SAVING;
        const config = {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
        axios.post('/api/upload', formData, config)
          .then(wait(1500), this.loading = true) // DEV ONLY: wait for 1.5s
          .then(x => {
            debugger
            this.uploadedFiles = [].concat(x);
            this.currentStatus = STATUS_SUCCESS;
            this.source = x.data.output_url
            this.loading = false;

          })
          .catch(err => {
            this.uploadError = err.response;
            this.currentStatus = STATUS_FAILED;
            this.loading = false;

          });
      },
      filesChange(fieldName, fileList) {
        // handle file changes
        const formData = new FormData();
        if (!fileList.length) return;
        // append the files to FormData
        formData.append('file', fileList[0])
        // save it
        this.save(formData);
      }
    },
    computed: {
      isInitial() {
        return this.currentStatus === STATUS_INITIAL;
      },
      isSaving() {
        return this.currentStatus === STATUS_SAVING;
      },
      isSuccess() {
        return this.currentStatus === STATUS_SUCCESS;
      },
      isFailed() {
        return this.currentStatus === STATUS_FAILED;
      }
    },
    mounted() {
      this.reset();
    },
    components: {
      ClipLoader,
      PacmanLoader,
      RingLoader
    }
  }

</script>

<style>
  .dropbox {
    outline: 2px dashed grey;
    /* the dash box */
    outline-offset: -10px;
    background: lightcyan;
    color: dimgray;
    padding: 10px 10px;
    min-height: 200px;
    /* minimum height */
    position: relative;
    cursor: pointer;
  }

  .input-file {
    opacity: 0;
    /* invisible but it's there! */
    width: 100%;
    height: 200px;
    position: absolute;
    cursor: pointer;
  }

  .dropbox:hover {
    background: lightblue;
    /* when mouse over to the drop zone, change color */
  }

  .dropbox p {
    font-size: 1.2em;
    text-align: center;
    padding: 50px 0;
  }

  .center {
    text-align: center;
    display: inline-block;
  }

</style>
